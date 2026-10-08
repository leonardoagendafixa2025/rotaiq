"""
ROTA IQ - Vercel Serverless Function Entrypoint
Exporta a aplicação FastAPI oficial para o runtime serverless da Vercel.
"""

import sys
import os

current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.dirname(current_dir)

for p in [root_dir, current_dir]:
    if p not in sys.path:
        sys.path.insert(0, p)

app = None

# Tentativa 1: Importar do backend principal
try:
    from backend.app.main import app as _app
    app = _app
except Exception as e1:
    # Tentativa 2: Importar da cópia de segurança em api._app
    try:
        from api._app.main import app as _app
        app = _app
    except Exception as e2:
        try:
            from _app.main import app as _app
            app = _app
        except Exception as e3:
            import traceback
            err_trace = traceback.format_exc()
            from fastapi import FastAPI
            from fastapi.responses import JSONResponse

            app = FastAPI(title="ROTA IQ Diagnostic Fallback")

            @app.get("/health")
            @app.get("/api/v1/health")
            @app.get("/{catchall:path}")
            async def fallback_handler(catchall: str = ""):
                return JSONResponse(
                    status_code=500,
                    content={
                        "status": "STARTUP_IMPORT_ERROR",
                        "error_backend": str(e1),
                        "error_api_app": str(e2),
                        "error_app": str(e3),
                        "traceback": err_trace,
                        "sys_path": sys.path
                    }
                )
