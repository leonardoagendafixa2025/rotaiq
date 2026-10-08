"""
ROTA IQ - Vercel Serverless Function Entrypoint
Exporta a aplicação ASGI FastAPI oficial para o runtime Python da Vercel.
"""

import sys
import os

# Adiciona os caminhos de importação necessários
current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.abspath(os.path.join(current_dir, ".."))

for path in [root_dir, current_dir]:
    if path not in sys.path:
        sys.path.insert(0, path)

try:
    from backend.app.main import app
except Exception:
    try:
        from api._app.main import app
    except Exception as e:
        import traceback
        err_msg = traceback.format_exc()
        from fastapi import FastAPI
        from fastapi.responses import HTMLResponse

        app = FastAPI(title="ROTA IQ Diagnostic Mode")

        @app.get("/{catchall:path}")
        async def diagnostic_view(catchall: str = ""):
            return HTMLResponse(
                f"<html><body style='background:#0f172a;color:#f8fafc;padding:2rem;font-family:sans-serif'>"
                f"<h2 style='color:#ef4444'>ROTA IQ API - Startup Diagnostic</h2>"
                f"<pre style='background:#1e293b;padding:1.5rem;border-radius:8px;overflow:auto'>{err_msg}</pre>"
                f"</body></html>",
                status_code=500
            )
