from fastapi import APIRouter, Form, UploadFile, File, HTTPException
from typing import Optional
from app.database import get_db_connection
from app.services import generate_tracking_code, compute_priority, save_upload_image

router = APIRouter(prefix="/api")

@router.get("/areas")
def get_areas():
    conn = get_db_connection()
    areas = conn.execute("SELECT id, area_name, ward_code, latitude, longitude FROM areas").fetchall()
    conn.close()
    return [{"id": a["id"], "area_name": a["area_name"], "ward_code": a["ward_code"], "latitude": a["latitude"], "longitude": a["longitude"]} for a in areas]

@router.post("/complaints")
async def create_complaint(
    reporter_name: str = Form(...),
    reporter_contact: Optional[str] = Form(None),
    area_id: int = Form(...),
    category: str = Form(...),
    description: str = Form(...),
    image: Optional[UploadFile] = File(None)
):
    valid_categories = {'Overflowing Bins', 'Littering', 'Plastic Waste', 'E-Waste', 'Hazardous Waste', 'Organic Waste'}
    if category not in valid_categories:
        raise HTTPException(status_code=400, detail="Invalid category")

    tracking_code = generate_tracking_code()
    priority, is_hotspot = compute_priority(category, area_id)
    image_url = await save_upload_image(image)

    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        INSERT INTO complaints (tracking_code, reporter_name, reporter_contact, area_id, category, description, image_url, priority, is_hotspot_escalated, status)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'Submitted')
    """, (tracking_code, reporter_name, reporter_contact, area_id, category, description, image_url, priority, is_hotspot))

    complaint_id = cursor.lastrowid

    cursor.execute("""
        INSERT INTO status_logs (complaint_id, old_status, new_status, remark)
        VALUES (?, NULL, 'Submitted', 'Complaint registered by resident')
    """, (complaint_id,))

    conn.commit()
    conn.close()

    return {"message": "Complaint submitted successfully", "tracking_code": tracking_code, "priority": priority, "is_hotspot_escalated": bool(is_hotspot)}

@router.get("/complaints/{tracking_code}")
def get_complaint(tracking_code: str):
    conn = get_db_connection()
    complaint = conn.execute("""
        SELECT c.id, c.tracking_code, c.reporter_name, a.area_name, c.category, c.description, c.image_url,
               c.priority, c.is_hotspot_escalated, c.status, c.assigned_team, c.created_at, c.resolved_at
        FROM complaints c
        JOIN areas a ON c.area_id = a.id
        WHERE c.tracking_code = ?
    """, (tracking_code,)).fetchone()

    if not complaint:
        conn.close()
        raise HTTPException(status_code=404, detail="Complaint not found")

    logs = conn.execute("""
        SELECT old_status, new_status, remark, updated_at
        FROM status_logs
        WHERE complaint_id = ?
        ORDER BY updated_at ASC
    """, (complaint["id"],)).fetchall()

    conn.close()

    return {
        "complaint": dict(complaint),
        "timeline": [dict(log) for log in logs]
    }

@router.get("/stats/public")
def get_public_stats():
    conn = get_db_connection()

    # Global counters
    total = conn.execute("SELECT COUNT(*) FROM complaints").fetchone()[0]
    pending = conn.execute("SELECT COUNT(*) FROM complaints WHERE status = 'Submitted'").fetchone()[0]
    in_progress = conn.execute("SELECT COUNT(*) FROM complaints WHERE status = 'In Progress'").fetchone()[0]
    resolved = conn.execute("SELECT COUNT(*) FROM complaints WHERE status = 'Resolved'").fetchone()[0]

    # Area summaries and hotspot flags
    area_stats = conn.execute("""
        SELECT a.area_name,
               COUNT(c.id) as total_reports,
               SUM(CASE WHEN c.status IN ('Submitted', 'In Progress') THEN 1 ELSE 0 END) as open_reports
        FROM areas a
        LEFT JOIN complaints c ON a.id = c.area_id
        GROUP BY a.id
    """).fetchall()

    conn.close()

    return {
        "counters": {"total": total, "pending": pending, "in_progress": in_progress, "resolved": resolved},
        "areas": [
            {
                "area_name": row["area_name"],
                "total_reports": row["total_reports"],
                "open_reports": row["open_reports"],
                "is_hotspot": row["open_reports"] >= 3
            } for row in area_stats
        ]
    }

@router.get("/awareness")
def get_awareness_info():
    # Searchable dictionary of everyday items and their correct bins
    return {
        "categories": {
            "Wet Waste (Green)": "Organic matter like food scraps, vegetable peels, fruit skins.",
            "Dry Waste (Blue)": "Recyclable items like paper, plastic bottles, cardboard, metal cans.",
            "Hazardous Waste (Red)": "Dangerous materials like batteries, paints, chemicals, broken glass.",
            "E-Waste (Yellow/Special)": "Electronic items like old phones, chargers, cables, circuit boards."
        },
        "items": {
            "banana peel": {"bin": "Wet Waste (Green)", "tip": "Ideal for composting."},
            "pizza box": {"bin": "Dry Waste (Blue)", "tip": "Clean cardboard only; tear off greasy sections for Wet Waste."},
            "milk carton": {"bin": "Dry Waste (Blue)", "tip": "Rinse out completely and flatten before discarding."},
            "battery": {"bin": "Hazardous Waste (Red)", "tip": "Do not puncture or expose to heat; tape terminals if possible."},
            "broken mirror": {"bin": "Hazardous Waste (Red)", "tip": "Wrap securely in newspaper to prevent injury to sanitation workers."},
            "charger": {"bin": "E-Waste (Yellow/Special)", "tip": "Coil wires to prevent tangling with other e-waste."},
            "old smartphone": {"bin": "E-Waste (Yellow/Special)", "tip": "Wipe personal data and remove SIM/SD cards before disposal."},
            "plastic bottle": {"bin": "Dry Waste (Blue)", "tip": "Empty contents, crush to save space, and keep cap on."},
            "medicine": {"bin": "Hazardous Waste (Red)", "tip": "Keep in original packaging; do not flush down the toilet."},
            "food scraps": {"bin": "Wet Waste (Green)", "tip": "Drain excess liquids; wrap in newspaper if necessary."},
            "plastic bag": {"bin": "Dry Waste (Blue)", "tip": "Ensure it is empty and clean; bundle multiple bags together."},
            "coffee grounds": {"bin": "Wet Waste (Green)", "tip": "Excellent for soil; can be mixed directly into compost."},
            "paint can": {"bin": "Hazardous Waste (Red)", "tip": "Dry out leftover paint completely before discarding."}
        }
    }
