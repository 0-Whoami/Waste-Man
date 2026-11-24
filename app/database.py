import sqlite3
import os

DB_DIR = "data"
DB_PATH = os.path.join(DB_DIR, "waste_management.db")

def get_db_connection():
    os.makedirs(DB_DIR, exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn

def init_db():
    os.makedirs(DB_DIR, exist_ok=True)
    conn = get_db_connection()
    cursor = conn.cursor()

    # Users table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS users (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        role TEXT NOT NULL CHECK(role IN ('resident', 'admin')),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    """)

    # Areas table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS areas (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        area_name TEXT UNIQUE NOT NULL,
        ward_code TEXT NOT NULL,
        latitude REAL NOT NULL,
        longitude REAL NOT NULL
    );
    """)

    # Complaints table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS complaints (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        tracking_code TEXT UNIQUE NOT NULL,
        reporter_name TEXT NOT NULL,
        reporter_contact TEXT,
        user_id INTEGER NULL REFERENCES users(id) ON DELETE SET NULL,
        area_id INTEGER NOT NULL REFERENCES areas(id) ON DELETE RESTRICT,
        category TEXT NOT NULL CHECK(category IN ('Overflowing Bins', 'Littering', 'Plastic Waste', 'E-Waste', 'Hazardous Waste', 'Organic Waste')),
        description TEXT NOT NULL,
        image_url TEXT NULL,
        priority TEXT NOT NULL CHECK(priority IN ('Low', 'Medium', 'High')),
        is_hotspot_escalated INTEGER NOT NULL DEFAULT 0,
        status TEXT NOT NULL DEFAULT 'Submitted' CHECK(status IN ('Submitted', 'In Progress', 'Resolved')),
        assigned_team TEXT DEFAULT 'Unassigned',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        resolved_at TIMESTAMP NULL
    );
    """)

    # Status Logs table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS status_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        complaint_id INTEGER NOT NULL REFERENCES complaints(id) ON DELETE CASCADE,
        updated_by INTEGER NULL REFERENCES users(id) ON DELETE SET NULL,
        old_status TEXT NULL,
        new_status TEXT NOT NULL CHECK(new_status IN ('Submitted', 'In Progress', 'Resolved')),
        remark TEXT NOT NULL,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
    """)

    conn.commit()
    conn.close()
