import os
import random
import uuid
from fastapi import UploadFile, HTTPException
from app.database import get_db_connection

UPLOAD_DIR = "static/uploads"
ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}
MAX_FILE_SIZE = 3 * 1024 * 1024 # 3 MB

def generate_tracking_code() -> str:
    conn = get_db_connection()
    while True:
        code = f"WM-{random.randint(1000, 9999)}"
        existing = conn.execute("SELECT id FROM complaints WHERE tracking_code = ?", (code,)).fetchone()
        if not existing:
            conn.close()
            return code

def compute_priority(category: str, area_id: int) -> tuple[str, int]:
    # Base priority by category
    if category in ('Hazardous Waste', 'E-Waste'):
        base_priority = 'High'
    elif category in ('Overflowing Bins', 'Plastic Waste'):
        base_priority = 'Medium'
    else:
        base_priority = 'Low'

    # Hotspot rule: count open complaints in the same area
    conn = get_db_connection()
    open_count = conn.execute(
        "SELECT COUNT(*) FROM complaints WHERE area_id = ? AND status IN ('Submitted', 'In Progress')",
        (area_id,)
    ).fetchone()[0]
    conn.close()

    is_hotspot = 1 if open_count >= 3 else 0
    priority = 'High' if is_hotspot else base_priority
    return priority, is_hotspot

async def save_upload_image(file: UploadFile | None) -> str | None:
    if not file or not file.filename:
        return None

    ext = os.path.splitext(file.filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(status_code=400, detail="Invalid file type. Allowed: .jpg, .jpeg, .png, .webp")

    contents = await file.read()
    if len(contents) > MAX_FILE_SIZE:
        raise HTTPException(status_code=400, detail="File size exceeds 3 MB limit")

    os.makedirs(UPLOAD_DIR, exist_ok=True)
    filename = f"{uuid.uuid4()}{ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    with open(filepath, "wb") as f:
        f.write(contents)

    return f"/static/uploads/{filename}"

def update_complaint_status_logic(complaint_id: int, new_status: str, assigned_team: str, remark: str, admin_id: int | None):
    conn = get_db_connection()
    complaint = conn.execute("SELECT id, status FROM complaints WHERE id = ?", (complaint_id,)).fetchone()
    if not complaint:
        conn.close()
        raise HTTPException(status_code=404, detail="Complaint not found")

    old_status = complaint["status"]

    # Validate state machine transitions
    valid = False
    if old_status == 'Submitted' and new_status in ('In Progress', 'Resolved'):
        valid = True
    elif old_status == 'In Progress' and new_status in ('Resolved', 'Submitted'):
        valid = True
    elif old_status == 'Resolved' and new_status in ('In Progress', 'Submitted'):
        valid = True

    if not valid and old_status != new_status:
        conn.close()
        raise HTTPException(status_code=400, detail=f"Invalid status transition from {old_status} to {new_status}")

    resolved_at_sql = "CURRENT_TIMESTAMP" if new_status == 'Resolved' else ("NULL" if old_status == 'Resolved' else "resolved_at")

    conn.execute(f"""
        UPDATE complaints
        SET status = ?, assigned_team = ?, resolved_at = {resolved_at_sql}
        WHERE id = ?
    """, (new_status, assigned_team, complaint_id))

    conn.execute("""
        INSERT INTO status_logs (complaint_id, updated_by, old_status, new_status, remark)
        VALUES (?, ?, ?, ?, ?)
    """, (complaint_id, admin_id, old_status, new_status, remark))

    conn.commit()
    conn.close()
