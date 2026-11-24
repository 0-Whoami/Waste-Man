# CleanTrack - Waste Management System Prototype

CleanTrack is a full-stack web prototype designed for rapid deployment during a hackathon. It allows residents to easily report waste issues and enables administrators to track, manage, and resolve them using a priority-driven hotspot engine.

## 🚀 Features
- **Frictionless Reporting**: Residents can submit issues without creating an account.
- **Smart Hotspot Engine**: Automatically escalates priority if an area accumulates 3 or more unresolved complaints.
- **Interactive Admin Dashboard**: Includes Chart.js analytics, a Leaflet.js interactive map, and a filterable ticket management table.
- **Secure Authentication**: Uses PBKDF2 hashed passwords and HTTP-only signed session cookies for the Admin Portal.
- **Zero External Paid APIs**: Runs 100% locally with SQLite, OpenStreetMap tiles (via Leaflet), and local image upload storage.

---

## 💻 Tech Stack
- **Backend:** Python 3.10+, FastAPI, Uvicorn, SQLite3 (No heavy ORMs).
- **Frontend:** HTML5, Tailwind CSS (via CDN), Vanilla JavaScript, Chart.js, Leaflet.js.
- **Security:** `hashlib` (PBKDF2/SHA256), `itsdangerous` (signed tokens), strict file upload validation.

---

## 🛠️ Setup & Run Instructions

1. **Install Dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

2. **Run the Application:**
   ```bash
   uvicorn app.main:app --reload
   ```
   *Note: If port 8000 is in use, specify a different port (e.g., `--port 8082`).*

3. **Access the Portals:**
   - **Resident Portal:** [http://127.0.0.1:8000/](http://127.0.0.1:8000/)
   - **Admin Dashboard:** [http://127.0.0.1:8000/admin](http://127.0.0.1:8000/admin)

---

## 🧪 Demo Credentials & Pre-seeded Data
Upon the first run, the database (`data/waste_management.db`) is automatically generated and seeded with:
- **6 Realistic Areas** (Wards).
- **18 Sample Complaints** (covering all categories and statuses, ensuring at least two areas trigger the Hotspot flag).
- **Auto-generated SVG sample images** for visual testing without missing file errors.

**Admin Login:**
- **Email:** `admin@cleantrack.demo`
- **Password:** `Admin@123`

*(A quick-fill button is available on the admin login screen for convenience).*

---

## ✅ Smoke-Test Checklist (Before Demo)
1. [ ] **Resident Submission:** Submit a new complaint on the home page with a mock image. Verify the tracking ID is generated and stats update.
2. [ ] **Tracking Code Search:** Copy the generated `WM-XXXX` code, paste it into the "Track Status" tab, and verify the timeline and details appear.
3. [ ] **Admin Login:** Go to `/admin`, click "Fill Demo Credentials", and log in.
4. [ ] **Map & Charts:** Verify the area chart and Leaflet map render correctly with the pre-seeded hotspot areas (red indicators).
5. [ ] **Status Update:** Click "Inspect" on a "Submitted" ticket in the admin table. Change status to "In Progress", assign a team, add a remark, and click Save. Verify the table and charts update instantly.

---

## 📜 Disclosures
* **Mock Data:** This prototype utilizes pre-seeded simulated data (complaints, areas, users) to demonstrate functionality.
* **Libraries:** Uses open-source CDN libraries (Tailwind, Chart.js, Leaflet). No paid APIs (like Google Maps or AWS S3) are required or used.
