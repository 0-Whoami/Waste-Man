import hashlib
import os
from fastapi import Request, HTTPException, Security, Depends
from itsdangerous import URLSafeTimedSerializer, BadSignature, SignatureExpired
from app.database import get_db_connection

SECRET_KEY = os.getenv("SECRET_KEY", "cleantrack-secret-dev-key-991")
serializer = URLSafeTimedSerializer(SECRET_KEY, salt="cleantrack-auth-salt")

def hash_password(password: str) -> str:
    salt = os.urandom(16).hex()
    pwd_hash = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), bytes.fromhex(salt), 100000).hex()
    return f"{salt}${pwd_hash}"

def verify_password(stored_password: str, provided_password: str) -> bool:
    try:
        salt, pwd_hash = stored_password.split('$')
        verify_hash = hashlib.pbkdf2_hmac('sha256', provided_password.encode('utf-8'), bytes.fromhex(salt), 100000).hex()
        return pwd_hash == verify_hash
    except Exception:
        return False

def create_session_token(user_id: int, email: str, role: str) -> str:
    return serializer.dumps({"user_id": user_id, "email": email, "role": role})

def verify_session_token(token: str) -> dict:
    try:
        data = serializer.loads(token, max_age=86400) # 24 hours
        return data
    except (BadSignature, SignatureExpired):
        raise HTTPException(status_code=401, detail="Session invalid or expired")

def require_admin(request: Request) -> dict:
    token = request.cookies.get("cleantrack_session")
    if not token:
        raise HTTPException(status_code=401, detail="Authentication required")
    data = verify_session_token(token)
    if data.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Admin privileges required")

    # Verify user exists in DB
    conn = get_db_connection()
    user = conn.execute("SELECT id, name, email, role FROM users WHERE id = ?", (data["user_id"],)).fetchone()
    conn.close()
    if not user:
        raise HTTPException(status_code=401, detail="User no longer exists")

    return {"user_id": user["id"], "email": user["email"], "name": user["name"], "role": user["role"]}
