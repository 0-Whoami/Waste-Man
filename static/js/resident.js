document.addEventListener('DOMContentLoaded', () => {
    if(window.location.pathname !== '/') return;

    loadAreas();
    loadPublicStats();
    loadAwarenessSearch();

    // Tab Switching
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('border-emerald-500', 'text-emerald-500'));
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.add('border-transparent', 'text-slate-400'));

            e.target.classList.remove('border-transparent', 'text-slate-400');
            e.target.classList.add('border-emerald-500', 'text-emerald-500');

            const targetId = e.target.getAttribute('data-target');
            document.querySelectorAll('.tab-content').forEach(c => c.classList.add('hidden'));
            document.getElementById(targetId).classList.remove('hidden');
        });
    });

    // Image Preview
    const imageInput = document.getElementById('image');
    if(imageInput) {
        imageInput.addEventListener('change', function() {
            const preview = document.getElementById('image-preview');
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    preview.src = e.target.result;
                    preview.classList.remove('hidden');
                };
                reader.readAsDataURL(this.files[0]);
            } else {
                preview.classList.add('hidden');
            }
        });
    }

    // Report Submission
    const reportForm = document.getElementById('report-form');
    if(reportForm) {
        reportForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = reportForm.querySelector('button[type="submit"]');
            btn.disabled = true;
            btn.textContent = 'Submitting...';

            const formData = new FormData(reportForm);
            try {
                const res = await API.postForm('/api/complaints', formData);

                document.getElementById('report-form-container').classList.add('hidden');
                const successDiv = document.getElementById('report-success');
                successDiv.classList.remove('hidden');
                document.getElementById('success-tracking-id').textContent = res.tracking_code;

                if(res.is_hotspot_escalated) {
                    document.getElementById('success-hotspot-msg').classList.remove('hidden');
                }

                reportForm.reset();
                document.getElementById('image-preview').classList.add('hidden');
                loadPublicStats(); // refresh counts
            } catch (err) {
                // error handled by API toast
            } finally {
                btn.disabled = false;
                btn.textContent = 'Submit Report';
            }
        });
    }

    // Tracking Search
    const trackForm = document.getElementById('track-form');
    if(trackForm) {
        trackForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const code = document.getElementById('tracking-input').value.trim();
            if(code) fetchTrackingDetails(code);
        });
    }
});

async function loadAreas() {
    const select = document.getElementById('area_id');
    if(!select) return;
    try {
        const areas = await API.get('/api/areas');
        areas.forEach(a => {
            const opt = document.createElement('option');
            opt.value = a.id;
            opt.textContent = `${a.area_name} (${a.ward_code})`;
            select.appendChild(opt);
        });
    } catch(e) {}
}

async function loadPublicStats() {
    try {
        const stats = await API.get('/api/stats/public');
        document.getElementById('stat-total').textContent = stats.counters.total;
        document.getElementById('stat-active').textContent = stats.counters.in_progress;
        document.getElementById('stat-resolved').textContent = stats.counters.resolved;
    } catch(e) {}
}

async function fetchTrackingDetails(code) {
    try {
        const data = await API.get(`/api/complaints/${code}`);
        const c = data.complaint;

        document.getElementById('track-result').classList.remove('hidden');
        document.getElementById('detail-id').textContent = c.tracking_code;
        document.getElementById('detail-area').textContent = c.area_name;
        document.getElementById('detail-category').textContent = c.category;
        document.getElementById('detail-desc').textContent = c.description;
        document.getElementById('detail-team').textContent = c.assigned_team;

        // Status Badge
        const sb = document.getElementById('detail-status');
        sb.textContent = c.status;
        sb.className = `badge badge-${c.status.toLowerCase().replace(' ', '')}`;

        // Stepper Visuals
        const steps = ['Submitted', 'In Progress', 'Resolved'];
        const currentIndex = steps.indexOf(c.status);
        steps.forEach((step, idx) => {
            const el = document.getElementById(`step-${idx+1}`);
            if(idx <= currentIndex) {
                el.classList.add('bg-emerald-500');
                el.classList.remove('bg-slate-700');
            } else {
                el.classList.add('bg-slate-700');
                el.classList.remove('bg-emerald-500');
            }
        });

        // Image
        const img = document.getElementById('detail-image');
        if(c.image_url) {
            img.src = c.image_url;
            img.classList.remove('hidden');
        } else {
            img.classList.add('hidden');
        }

        // Timeline
        const tlDiv = document.getElementById('detail-timeline');
        tlDiv.innerHTML = '';
        data.timeline.forEach(log => {
            const d = new Date(log.updated_at).toLocaleString();
            tlDiv.innerHTML += `
                <div class="relative pl-6 pb-4 border-l border-slate-600 last:border-0 last:pb-0">
                    <div class="absolute w-3 h-3 bg-emerald-500 rounded-full -left-[6.5px] top-1"></div>
                    <div class="text-sm font-semibold">${log.new_status}</div>
                    <div class="text-xs text-slate-400 mb-1">${d}</div>
                    <div class="text-sm text-slate-300 italic">${log.remark}</div>
                </div>
            `;
        });

    } catch(e) {
        document.getElementById('track-result').classList.add('hidden');
    }
}

function fillSampleTracking(code) {
    document.getElementById('tracking-input').value = code;
    fetchTrackingDetails(code);
}

// Awareness Search
let awarenessData = null;
async function loadAwarenessSearch() {
    const input = document.getElementById('awareness-search');
    const resultDiv = document.getElementById('awareness-result');
    if(!input) return;

    try {
        awarenessData = await API.get('/api/awareness');
    } catch(e) {}

    input.addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        if(query.length < 2) {
            resultDiv.innerHTML = '';
            return;
        }

        const matches = Object.entries(awarenessData.items).filter(([item]) => item.includes(query));

        if(matches.length === 0) {
            resultDiv.innerHTML = `<div class="p-3 text-slate-400">No specific match. Try general categories above.</div>`;
            return;
        }

        let html = '<div class="space-y-2 mt-2">';
        matches.forEach(([item, bin]) => {
            let color = 'bg-slate-700 text-slate-200';
            if(bin.includes('Green')) color = 'bg-emerald-900/50 text-emerald-400 border border-emerald-700';
            if(bin.includes('Blue')) color = 'bg-blue-900/50 text-blue-400 border border-blue-700';
            if(bin.includes('Red')) color = 'bg-red-900/50 text-red-400 border border-red-700';
            if(bin.includes('Yellow')) color = 'bg-yellow-900/50 text-yellow-400 border border-yellow-700';

            html += `<div class="p-3 rounded flex justify-between items-center ${color}">
                <span class="capitalize font-medium">${item}</span>
                <span class="text-sm font-bold">${bin}</span>
            </div>`;
        });
        html += '</div>';
        resultDiv.innerHTML = html;
    });
}
