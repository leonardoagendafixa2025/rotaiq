import os
import sys

# Garante que as pastas backend e raiz do projeto estejam no sys.path do runtime do Vercel
current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.abspath(os.path.join(current_dir, ".."))
backend_dir = os.path.join(root_dir, "backend")

for p in [backend_dir, root_dir, current_dir]:
    if p not in sys.path:
        sys.path.insert(0, p)

# Configuração padrão do ambiente se não definidos
if not os.environ.get("SUPABASE_URL"):
    os.environ["SUPABASE_URL"] = "https://jkreduqzekllsmzxiugn.supabase.co"
if not os.environ.get("ENVIRONMENT"):
    os.environ["ENVIRONMENT"] = "production"

try:
    from app.main import app
except Exception as err:
    try:
        from backend.app.main import app
    except Exception as err2:
        from fastapi import FastAPI
        from fastapi.responses import JSONResponse
        app = FastAPI(title="ROTA IQ - Fallback")
        @app.api_route("/{full_path:path}", methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"])
        async def emergency_fallback(full_path: str):
            return JSONResponse(
                status_code=500,
                content={
                    "error": "FastAPI initialization failed on Vercel",
                    "error_primary": str(err),
                    "error_secondary": str(err2),
                    "path_attempted": full_path,
                    "sys_path": sys.path
                }
            )
