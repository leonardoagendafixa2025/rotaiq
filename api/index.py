from fastapi import FastAPI
from fastapi.responses import HTMLResponse, JSONResponse
import os
import sys
import traceback

app = FastAPI(title="ROTA IQ Diagnostic")

@app.get("/")
def root():
    return {"message": "ROTA IQ System Online", "python": sys.version}

@app.get("/health")
@app.get("/api/v1/health")
def health():
    return {"status": "online", "python": sys.version, "cwd": os.getcwd()}

@app.get("/admin", response_class=HTMLResponse)
@app.get("/admin/", response_class=HTMLResponse)
def admin():
    return "<h1>ROTA IQ ADMIN WORKS!</h1><p>Vercel routing to FastAPI is 100% active.</p>"

@app.get("/test-import")
def test_import():
    current_dir = os.path.dirname(os.path.abspath(__file__))
    root_dir = os.path.abspath(os.path.join(current_dir, ".."))
    backend_dir = os.path.join(root_dir, "backend")
    for p in [current_dir, backend_dir, root_dir]:
        if p not in sys.path:
            sys.path.insert(0, p)
    
    results = {}
    try:
        import api._app.main as m
        results["api._app.main"] = "SUCCESS"
    except Exception:
        results["api._app.main"] = traceback.format_exc()

    try:
        import backend.app.main as bm
        results["backend.app.main"] = "SUCCESS"
    except Exception:
        results["backend.app.main"] = traceback.format_exc()

    return results
