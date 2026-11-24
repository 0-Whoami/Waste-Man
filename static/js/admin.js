let map = null;
let currentChart = null;

document.addEventListener('DOMContentLoaded', async () => {
    if(window.location.pathname !== '/admin') return;

    // Check Auth State
    try {
        await API.get('/api/auth/me');
        showDashboard();
    } catch(e) {
        showLogin();
    }

    // Login Form
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        try {
            await API.post('/api/auth/login', {email, password});
            showDashboard();
        } catch(e) {}
    });

    document.getElementById('btn-demo-creds').addEventListener('click', () => {
        document.getElementById('email').value = 'admin@cleantrack.demo';
        document.getElementById('password').value = 'Admin@123';
    });

    document.getElementById('logout-btn').addEventListener('click', async () => {
        await API.post('/api/auth/logout');
        location.reload();
    });

    // Filters
    document.querySelectorAll('.filter-input').forEach(el => {
        el.addEventListener('change', loadTable);
    });
    document.getElementById('filter-search').addEventListener('input', debounce(loadTable, 500));

    // Export CSV
    const exportBtn = document.getElementById('btn-export-csv');
    if (exportBtn) {
        exportBtn.addEventListener('click', exportCSV);
    }

    // Modal close
    document.getElementById('close-modal').addEventListener('click', () => {
        document.getElementById('update-modal').classList.add('hidden');
    });

    // Update Form
    document.getElementById('update-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = document.getElementById('modal-id').value;
        const status = document.getElementById('modal-status').value;
        const assigned_team = document.getElementById('modal-team').value;
        const remark = document.getElementById('modal-remark').value;

        try {
            await API.patch(`/api/admin/complaints/${id}`, {status, assigned_team, remark});
            Toast.show("Updated successfully");
            document.getElementById('update-modal').classList.add('hidden');
            loadDashboardData();
        } catch(e) {}
    });
});

function showLogin() {
    document.getElementById('login-section').classList.remove('hidden');
    document.getElementById('dashboard-section').classList.add('hidden');
}

function showDashboard() {
    document.getElementById('login-section').classList.add('hidden');
    document.getElementById('dashboard-section').classList.remove('hidden');

    initMap();
    loadDashboardData();
}

async function loadDashboardData() {
    await Promise.all([loadAnalytics(), loadTable(), loadFilterAreas()]);
}

async function loadFilterAreas() {
    try {
        const areas = await API.get('/api/areas');
        const sel = document.getElementById('filter-area');
        if(sel.options.length <= 1) { // only load once
            areas.forEach(a => {
                const opt = document.createElement('option');
                opt.value = a.id;
                opt.textContent = a.area_name;
                sel.appendChild(opt);
            });
        }
    } catch(e) {}
}

async function loadAnalytics() {
    try {
        const data = await API.get('/api/admin/analytics');

        // KPIs
        document.getElementById('kpi-total').textContent = data.kpis.total;
        document.getElementById('kpi-pending').textContent = data.kpis.pending;
        document.getElementById('kpi-active').textContent = data.kpis.active;
        document.getElementById('kpi-resolved').textContent = data.kpis.resolved;
        document.getElementById('kpi-hotspots').textContent = data.kpis.hotspots_count;

        updateMap(data.area_stats);
        updateChart(data.area_stats);
    } catch(e) {}
}

let currentRecords = [];

async function loadTable() {
    const status = document.getElementById('filter-status').value;
    const area_id = document.getElementById('filter-area').value;
    const category = document.getElementById('filter-category').value;
    const priority = document.getElementById('filter-priority').value;
    const search = document.getElementById('filter-search').value;

    let q = new URLSearchParams();
    if(status) q.append('status', status);
    if(area_id) q.append('area_id', area_id);
    if(category) q.append('category', category);
    if(priority) q.append('priority', priority);
    if(search) q.append('search', search);

    try {
        const records = await API.get(`/api/admin/complaints?${q.toString()}`);
        currentRecords = records;
        const tbody = document.getElementById('table-body');
        tbody.innerHTML = '';

        if(records.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" class="p-4 text-center text-slate-400">No records found.</td></tr>`;
            return;
        }

        records.forEach(r => {
            const d = new Date(r.created_at).toLocaleDateString();
            const badgeClass = `badge status-${r.status.toLowerCase().replace(/\s+/g, '-')}`;

            let prioClass = `badge priority-${r.priority.toLowerCase()}`;
            let prioText = r.priority;

            if (r.is_hotspot_escalated) {
                if (r.status === 'Resolved') {
                    prioClass = 'badge badge-muted';
                    prioText = 'WAS HOTSPOT';
                } else {
                    prioClass = 'badge badge-hotspot';
                    prioText = '🔥 HOTSPOT';
                }
            }

            const tr = document.createElement('tr');
            tr.className = "border-b border-slate-700/50 hover:bg-slate-800/50 transition-colors";
            tr.innerHTML = `
                <td class="p-3 font-mono text-sm text-emerald-400">${r.tracking_code}</td>
                <td class="p-3 text-sm">${r.area_name}</td>
                <td class="p-3 text-sm">${r.category}</td>
                <td class="p-3"><span class="${prioClass}">${prioText}</span></td>
                <td class="p-3"><span class="${badgeClass}">${r.status}</span></td>
                <td class="p-3 text-sm text-slate-300">${r.assigned_team || 'Unassigned'}</td>
                <td class="p-3 text-sm text-slate-400">${d}</td>
                <td class="p-3">
                    <button onclick="openModal('${r.tracking_code}')" class="text-sm bg-slate-700 hover:bg-slate-600 px-3 py-1 rounded transition-colors">Inspect</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch(e) {}
}

async function openModal(code) {
    try {
        const data = await API.get(`/api/complaints/${code}`);
        const c = data.complaint;

        document.getElementById('modal-tracking-id').textContent = c.tracking_code;
        document.getElementById('modal-area').textContent = c.area_name;
        document.getElementById('modal-category').textContent = c.category;
        document.getElementById('modal-desc').textContent = c.description;

        const img = document.getElementById('modal-image');
        if(c.image_url) {
            img.src = c.image_url;
            img.classList.remove('hidden');
        } else {
            img.classList.add('hidden');
        }

        // Form bindings
        document.getElementById('modal-id').value = c.id;
        document.getElementById('modal-status').value = c.status;
        document.getElementById('modal-team').value = c.assigned_team || 'Unassigned';
        document.getElementById('modal-remark').value = '';

        // Timeline
        const tlDiv = document.getElementById('modal-timeline');
        tlDiv.innerHTML = '';
        data.timeline.forEach(log => {
            const d = new Date(log.updated_at).toLocaleString();
            tlDiv.innerHTML += `
                <div class="mb-2 p-2 bg-slate-800 rounded text-sm border-l-2 border-emerald-500">
                    <div class="flex justify-between text-slate-400 text-xs mb-1">
                        <span>${log.new_status}</span>
                        <span>${d}</span>
                    </div>
                    <div class="text-slate-200">${log.remark}</div>
                </div>
            `;
        });

        document.getElementById('update-modal').classList.remove('hidden');
    } catch(e) {}
}

function initMap() {
    if(map) return;
    map = L.map('map-container').setView([12.9716, 77.5946], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
    }).addTo(map);
}

function updateMap(areaStats) {
    if(!map) return;
    // Clear existing markers
    map.eachLayer(layer => {
        if(layer instanceof L.CircleMarker) map.removeLayer(layer);
    });

    let markers = [];
    areaStats.forEach(a => {
        const color = a.open_reports >= 3 ? '#ef4444' : (a.open_reports > 0 ? '#f59e0b' : '#10b981');
        const radius = Math.max(8, a.open_reports * 4);

        const isHotspot = a.open_reports >= 3;
        const statusText = isHotspot ? '<span class="text-red-500 font-bold">🔥 HOTSPOT</span>' : (a.open_reports > 0 ? 'Active Issues' : 'Clear');

        const popupContent = `
            <div class="text-sm">
                <strong class="text-white text-base block mb-1 border-b border-slate-700 pb-1">${a.area_name}</strong>
                <div class="text-slate-300">Open Reports: <span class="font-bold text-white">${a.open_reports}</span></div>
                <div class="text-slate-300">Resolved: <span class="font-bold text-white">${a.resolved_reports}</span></div>
                <div class="mt-2">${statusText}</div>
            </div>
        `;

        const marker = L.circleMarker([a.latitude, a.longitude], {
            color: color,
            fillColor: color,
            fillOpacity: 0.6,
            radius: radius
        }).bindPopup(popupContent);

        marker.addTo(map);
        markers.push(marker);
    });

    if (markers.length > 0) {
        const group = new L.featureGroup(markers);
        map.fitBounds(group.getBounds(), { padding: [30, 30], maxZoom: 14 });
    }
}

function updateChart(areaStats) {
    const ctx = document.getElementById('areaChart').getContext('2d');

    const labels = areaStats.map(a => a.area_name);
    const openData = areaStats.map(a => a.open_reports);
    const resolvedData = areaStats.map(a => a.resolved_reports);

    if(currentChart) currentChart.destroy();

    currentChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Hotspot Open (3+)',
                    data: openData.map(v => v >= 3 ? v : 0),
                    backgroundColor: '#ef4444',
                },
                {
                    label: 'Normal Open',
                    data: openData.map(v => v > 0 && v < 3 ? v : 0),
                    backgroundColor: '#f59e0b',
                },
                {
                    label: 'Resolved',
                    data: resolvedData,
                    backgroundColor: '#10b981',
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { stacked: true, grid: { color: '#334155' }, ticks:{color:'#94a3b8'} },
                y: { stacked: true, grid: { color: '#334155' }, ticks:{color:'#94a3b8'} }
            },
            plugins: {
                legend: { labels: { color: '#f8fafc' } }
            }
        }
    });
}

function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

function exportCSV() {
    if (currentRecords.length === 0) {
        Toast.show("No records to export", "error");
        return;
    }
    const headers = ['Tracking ID', 'Area', 'Category', 'Priority', 'Status', 'Assigned Team', 'Created Date', 'Description'];
    let csvContent = headers.join(',') + '\n';

    currentRecords.forEach(r => {
        const d = new Date(r.created_at).toLocaleDateString();
        const row = [
            r.tracking_code,
            r.area_name,
            r.category,
            r.priority,
            r.status,
            r.assigned_team || 'Unassigned',
            d,
            r.description || ''
        ].map(field => {
            const escaped = String(field).replace(/"/g, '""');
            return `"${escaped}"`;
        });
        csvContent += row.join(',') + '\n';
    });

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "cleantrack_reports.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}
