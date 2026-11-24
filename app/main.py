import os
from fastapi import FastAPI, Request
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from fastapi.responses import HTMLResponse
from contextlib import asynccontextmanager

from app.database import init_db
from app.seed import seed_database
from app.routes import public_routes, admin_routes

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: Initialize DB and seed demo data
    init_db()
    seed_database()
    yield
    # Shutdown logic (if any)

app = FastAPI(title="CleanTrack Waste Management API", lifespan=lifespan)

# Mount static files (CSS, JS, Uploads)
os.makedirs("static", exist_ok=True)
os.makedirs("static/uploads", exist_ok=True)
app.mount("/static", StaticFiles(directory="static"), name="static")

templates = Jinja2Templates(directory="templates")

# Register API Routers
app.include_router(public_routes.router)
app.include_router(admin_routes.router)

# Template Routes
@app.get("/", response_class=HTMLResponse)
async def serve_resident_portal(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})

@app.get("/admin", response_class=HTMLResponse)
async def serve_admin_dashboard(request: Request):
    return templates.TemplateResponse("admin.html", {"request": request})
