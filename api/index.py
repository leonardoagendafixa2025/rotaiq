import os
import sys
import traceback

current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.abspath(os.path.join(current_dir, ".."))
backend_dir = os.path.join(root_dir, "backend")

for p in [backend_dir, current_dir, root_dir]:
    if p not in sys.path:
        sys.path.insert(0, p)

if not os.environ.get("SUPABASE_URL"):
    os.environ["SUPABASE_URL"] = "https://jkreduqzekllsmzxiugn.supabase.co"
if not os.environ.get("ENVIRONMENT"):
    os.environ["ENVIRONMENT"] = "production"

debug_errors = {}
main_app = None

# Attempt 1: backend.app.main
try:
    from backend.app.main import app as _app1
    main_app = _app1
    debug_errors["backend"] = "OK"
except Exception as e:
    debug_errors["backend"] = traceback.format_exc()

# Attempt 2: api.app.main
if not main_app:
    try:
        from api.app.main import app as _app2
        main_app = _app2
        debug_errors["api"] = "OK"
    except Exception as e:
        debug_errors["api"] = traceback.format_exc()

# Attempt 3: app.main
if not main_app:
    try:
        from app.main import app as _app3
        main_app = _app3
        debug_errors["app"] = "OK"
    except Exception as e:
        debug_errors["app"] = traceback.format_exc()

if main_app:
    app = main_app
    @app.get("/api/v1/debug-status")
    def debug_status():
        return {
            "status": "online",
            "debug_errors": debug_errors,
            "cwd": os.getcwd(),
            "sys_path": sys.path
        }
else:
    from fastapi import FastAPI
    from fastapi.responses import JSONResponse
    app = FastAPI(title="ROTA IQ - Fallback")

    @app.get("/")
    @app.get("/{full_path:path}")
    def fallback_catch_all(full_path: str = ""):
        return JSONResponse(
            status_code=200,
            content={
                "error": "FastAPI initialization failed on Vercel",
                "debug_errors": debug_errors,
                "cwd": os.getcwd(),
                "sys_path": sys.path
            }
        )
