import os
from app.database import get_db_connection
from app.auth import hash_password

def create_sample_svg(filename: str, text_label: str):
    os.makedirs("static/uploads", exist_ok=True)
    filepath = os.path.join("static/uploads", filename)
    if not os.path.exists(filepath):
        svg_content = f'''<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400">
  <rect width="100%" height="100%" fill="#0F172A"/>
  <rect x="50" y="50" width="500" height="300" rx="15" fill="#1E293B" stroke="#059669" stroke-width="4"/>
  <circle cx="300" cy="180" r="60" fill="#059669" opacity="0.2"/>
  <path d="M270 160 L300 210 L330 160 Z" fill="#059669"/>
  <text x="300" y="280" font-family="Arial, sans-serif" font-size="20" font-weight="bold" fill="#F8FAFC" text-anchor="middle">{text_label}</text>
  <text x="300" y="310" font-family="Arial, sans-serif" font-size="14" fill="#94A3B8" text-anchor="middle">CleanTrack Verified Sample Photo</text>
</svg>'''
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(svg_content)

def seed_database():
    conn = get_db_connection()
    cursor = conn.cursor()

    # Check if areas already seeded
    area_check = cursor.execute("SELECT COUNT(*) FROM areas").fetchone()[0]
    if area_check > 0:
        conn.close()
        return

    # 1. Seed 6 Areas
    areas_data = [
        ("Ward 1 - Central Market", "W01", 12.9716, 77.5946),
        ("Ward 2 - Riverside Colony", "W02", 12.9352, 77.6245),
        ("Ward 3 - Green Park", "W03", 12.9279, 77.5826),
        ("Ward 4 - Tech Sector", "W04", 12.9698, 77.7500),
        ("Ward 5 - Station Road", "W05", 12.9855, 77.5700),
        ("Ward 6 - Lakeview Heights", "W06", 12.9100, 77.5900)
    ]
    cursor.executemany("""
        INSERT INTO areas (area_name, ward_code, latitude, longitude)
        VALUES (?, ?, ?, ?)
    """, areas_data)

    # 2. Seed Users
    admin_pass = hash_password("Admin@123")
    resident_pass = hash_password("Resident@123")
    cursor.execute("""
        INSERT INTO users (name, email, password_hash, role)
        VALUES (?, ?, ?, ?)
    """, ("System Admin", "admin@cleantrack.demo", admin_pass, "admin"))

    cursor.execute("""
        INSERT INTO users (name, email, password_hash, role)
        VALUES (?, ?, ?, ?)
    """, ("Demo Resident", "resident@cleantrack.demo", resident_pass, "resident"))

    # 3. Create Sample SVGs
    create_sample_svg("sample_overflow.svg", "Overflowing Bins at Market")
    create_sample_svg("sample_plastic.svg", "Plastic Waste Accumulation")
    create_sample_svg("sample_ewate.svg", "Abandoned E-Waste / Gadgets")

    # 4. Seed 18 Realistic Complaints
    # Note: Ward 1 (area_id=1) and Ward 3 (area_id=3) will have >= 3 unresolved complaints to trigger Hotspots immediately.
    complaints_data = [
        # Ward 1 - Central Market (Hotspot: 4 open complaints)
        ("WM-1001", "Ramesh Kumar", "ramesh@example.com", 1, 'Overflowing Bins', "Large commercial dustbins spilling over near vegetable stalls.", "/static/uploads/sample_overflow.svg", "High", 1, 'Submitted', 'Unassigned'),
        ("WM-1002", "Priya Sharma", "priya@example.com", 1, 'Plastic Waste', "Bundles of plastic carry bags dumped near drainage.", "/static/uploads/sample_plastic.svg", "High", 1, 'In Progress', 'Crew Alpha'),
        ("WM-1003", "Anand Rao", None, 1, 'Organic Waste', "Rotting vegetable waste emitting foul odor.", None, "High", 1, 'Submitted', 'Unassigned'),
        ("WM-1004", "Sunita Rao", None, 1, 'Littering', "General paper and food wrapper litter all along sidewalk.", None, "High", 1, 'Submitted', 'Unassigned'),

        # Ward 2 - Riverside Colony (2 open, 1 resolved)
        ("WM-1005", "Kavita Menon", "kavita@example.com", 2, 'Hazardous Waste', "Discarded paint cans and chemical containers near water body.", None, "High", 0, 'In Progress', 'Hazardous Response Unit'),
        ("WM-1006", "Vikram Malhotra", None, 2, 'Plastic Waste', "Plastic water bottles clogged in stormwater drain.", None, "Medium", 0, 'Submitted', 'Unassigned'),
        ("WM-1007", "Deepak Joshi", None, 2, 'Overflowing Bins', "Residential wheelie bins left uncollected for 3 days.", None, "Low", 0, 'Resolved', 'Crew Beta'),

        # Ward 3 - Green Park (Hotspot: 3 open complaints)
        ("WM-1008", "Neha Gupta", "neha@example.com", 1, 'E-Waste', "Old CRT monitors and computer peripherals dumped in park.", "/static/uploads/sample_ewate.svg", "High", 1, 'Submitted', 'Unassigned'),
        ("WM-1009", "Rahul Verma", None, 3, 'Organic Waste', "Garden trimmings and leaf piles blocked walkway.", None, "High", 1, 'Submitted', 'Unassigned'),
        ("WM-1010", "Siddharth Roy", None, 3, 'Littering', "Plastic cups and snack packets scattered around playground.", None, "High", 1, 'In Progress', 'Crew Alpha'),

        # Ward 4 - Tech Sector (1 open, 1 resolved)
        ("WM-1011", "Anita Desai", "anita@example.com", 2, 'E-Waste', "Server racks and discarded keyboards near IT park backgate.", None, "High", 0, 'In Progress', 'E-Waste Unit'),
        ("WM-1012", "Kiran Kumar", None, 4, 'Plastic Waste', "Disposable coffee cups overflowing recycling bin.", None, "Medium", 0, 'Resolved', 'Crew Beta'),

        # Ward 5 - Station Road (2 open, 1 resolved)
        ("WM-1013", "Manoj Tiwari", None, 5, 'Overflowing Bins', "Platform dustbins overflowing near ticket counter.", None, "Medium", 0, 'Submitted', 'Unassigned'),
        ("WM-1014", "Pooja Hegde", None, 5, 'Hazardous Waste', "Broken medical vials near railway dispensary.", None, "High", 0, 'In Progress', 'Hazardous Response Unit'),
        ("WM-1015", "Arun Patil", None, 5, 'Littering', "Food packets thrown across railway tracks approach.", None, "Low", 0, 'Resolved', 'Crew Alpha'),

        # Ward 6 - Lakeview Heights (2 open, 1 resolved)
        ("WM-1016", "Smita Patil", "smita@example.com", 2, 'Plastic Waste', "Plastic packaging material dumped near jogging track.", None, "Medium", 0, 'Submitted', 'Unassigned'),
        ("WM-1017", "Nitin Gadkari", None, 6, 'Organic Waste', "Coconut shells and fruit waste dumped illegally.", None, "Low", 0, 'In Progress', 'Crew Beta'),
        ("WM-1018", "Rajeev Kumar", None, 6, 'Overflowing Bins', "Apartment complex communal bin overflowing.", None, "Low", 0, 'Resolved', 'Crew Alpha')
    ]

    for c in complaints_data:
        cursor.execute("""
            INSERT INTO complaints (tracking_code, reporter_name, reporter_contact, area_id, category, description, image_url, priority, is_hotspot_escalated, status, assigned_team)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, c)

        comp_id = cursor.lastrowid
        status = c[-2]
        team = c[-1]

        # Insert initial log
        cursor.execute("""
            INSERT INTO status_logs (complaint_id, updated_by, old_status, new_status, remark)
            VALUES (?, NULL, NULL, 'Submitted', 'Complaint registered by resident')
        """, (comp_id,))

        if status != 'Submitted':
            cursor.execute("""
                INSERT INTO status_logs (complaint_id, updated_by, old_status, new_status, remark)
                VALUES (?, 1, 'Submitted', ?, ?)
            """, (comp_id, status, f"Assigned to {team} and updated status"))

    conn.commit()
    conn.close()
