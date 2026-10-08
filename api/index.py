import os
import sys

current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.abspath(os.path.join(current_dir, ".."))
backend_dir = os.path.join(root_dir, "backend")

for p in [current_dir, backend_dir, root_dir]:
    if p not in sys.path:
        sys.path.insert(0, p)

if not os.environ.get("SUPABASE_URL"):
    os.environ["SUPABASE_URL"] = "https://jkreduqzekllsmzxiugn.supabase.co"
if not os.environ.get("ENVIRONMENT"):
    os.environ["ENVIRONMENT"] = "production"

try:
    from _app.main import app
except Exception:
    try:
        from api._app.main import app
    except Exception:
        from backend.app.main import app
