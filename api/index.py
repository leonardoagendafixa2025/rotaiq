"""
ROTA IQ - Vercel Serverless Function Entrypoint
Exporta a aplicação FastAPI oficial para o runtime Python da Vercel.
"""

import sys
import os

current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.abspath(os.path.join(current_dir, ".."))

for p in [current_dir, root_dir]:
    if p not in sys.path:
        sys.path.insert(0, p)

# Cascata de importação resiliente para garantir carregamento em qualquer contexto de empacotamento da Vercel
app = None

try:
    from _app.main import app
except Exception:
    pass

if app is None:
    try:
        from api._app.main import app
    except Exception:
        pass

if app is None:
    try:
        from backend.app.main import app
    except Exception:
        pass

if app is None:
    import traceback
    err_tb = traceback.format_exc()
    from fastapi import FastAPI
    from fastapi.responses import JSONResponse

    app = FastAPI(title="ROTA IQ Diagnostic Mode")

    @app.get("/")
    @app.get("/health")
    @app.get("/api/v1/health")
    @app.get("/{catchall:path}")
    async def fallback_diagnostic(catchall: str = ""):
        return JSONResponse(
            status_code=500,
            content={
                "error": "STARTUP_IMPORT_FAILED",
                "detail": err_tb,
                "sys_path": sys.path,
                "current_dir": current_dir,
                "root_dir": root_dir
            }
        )
