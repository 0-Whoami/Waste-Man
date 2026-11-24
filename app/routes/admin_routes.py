from fastapi import APIRouter, Depends, HTTPException, Request, Response
from typing import Optional
from app.database import get_db_connection
from app.schemas import AdminLogin, ComplaintUpdate
from app.auth import verify_password, create_session_token, require_admin
from app.services import update_complaint_status_logic

router = APIRouter(prefix="/api")

@router.post("/auth/login")
def login(creds: AdminLogin, response: Response):
    conn = get_db_connection()
    user = conn.execute("SELECT id, email, password_hash, role FROM users WHERE email = ?", (creds.email,)).fetchone()
    conn.close()

    if not user or not verify_password(user["password_hash"], creds.password):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    if user["role"] != "admin":
        raise HTTPException(status_code=403, detail="Admin access required")

    token = create_session_token(user["id"], user["email"], user["role"])
    response.set_cookie(key="cleantrack_session", value=token, httponly=True, max_age=86400, samesite="lax")
    return {"message": "Login successful"}

@router.post("/auth/logout")
def logout(response: Response):
    response.delete_cookie("cleantrack_session")
    return {"message": "Logged out"}

@router.get("/auth/me")
def current_user(admin: dict = Depends(require_admin)):
    return admin

@router.get("/admin/complaints")
def list_complaints(
    admin: dict = Depends(require_admin),
    status: Optional[str] = None,
    area_id: Optional[int] = None,
    category: Optional[str] = None,
    priority: Optional[str] = None,
    search: Optional[str] = None
):
    query = """
        SELECT c.id, c.tracking_code, a.area_name, c.category, c.priority, c.is_hotspot_escalated, c.status, c.assigned_team, c.created_at
        FROM complaints c
        JOIN areas a ON c.area_id = a.id
        WHERE 1=1
    """
    params = []

    if status:
        query += " AND c.status = ?"
        params.append(status)
    if area_id:
        query += " AND c.area_id = ?"
        params.append(area_id)
    if category:
        query += " AND c.category = ?"
        params.append(category)
    if priority:
        query += " AND c.priority = ?"
        params.append(priority)
    if search:
        query += " AND (c.tracking_code LIKE ? OR c.description LIKE ?)"
        params.extend([f"%{search}%", f"%{search}%"])

    query += " ORDER BY c.created_at DESC"

    conn = get_db_connection()
    complaints = conn.execute(query, params).fetchall()
    conn.close()

    return [dict(c) for c in complaints]

@router.patch("/admin/complaints/{complaint_id}")
def update_complaint_status(complaint_id: int, payload: ComplaintUpdate, admin: dict = Depends(require_admin)):
    update_complaint_status_logic(
        complaint_id=complaint_id,
        new_status=payload.status,
        assigned_team=payload.assigned_team,
        remark=payload.remark,
        admin_id=admin["user_id"]
    )
    return {"message": "Complaint updated successfully"}

@router.get("/admin/analytics")
def get_analytics(admin: dict = Depends(require_admin)):
    conn = get_db_connection()

    total = conn.execute("SELECT COUNT(*) FROM complaints").fetchone()[0]
    pending = conn.execute("SELECT COUNT(*) FROM complaints WHERE status = 'Submitted'").fetchone()[0]
    active = conn.execute("SELECT COUNT(*) FROM complaints WHERE status = 'In Progress'").fetchone()[0]
    resolved = conn.execute("SELECT COUNT(*) FROM complaints WHERE status = 'Resolved'").fetchone()[0]

    # Area-wise stats for chart and map
    area_stats = conn.execute("""
        SELECT a.id, a.area_name, a.latitude, a.longitude,
               SUM(CASE WHEN c.status IN ('Submitted', 'In Progress') THEN 1 ELSE 0 END) as open_reports,
               SUM(CASE WHEN c.status = 'Resolved' THEN 1 ELSE 0 END) as resolved_reports
        FROM areas a
        LEFT JOIN complaints c ON a.id = c.area_id
        GROUP BY a.id
    """).fetchall()

    hotspots_count = sum(1 for a in area_stats if a["open_reports"] >= 3)
    resolution_rate = round((resolved / total * 100) if total > 0 else 0, 1)

    category_stats = conn.execute("""
        SELECT category, COUNT(*) as count
        FROM complaints
        GROUP BY category
    """).fetchall()

    conn.close()

    return {
        "kpis": {
            "total": total,
            "pending": pending,
            "active": active,
            "resolved": resolved,
            "hotspots_count": hotspots_count,
            "resolution_rate": resolution_rate
        },
        "area_stats": [dict(a) for a in area_stats],
        "category_stats": {row["category"]: row["count"] for row in category_stats}
    }
