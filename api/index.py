import os
import sys

# Garante que o pacote backend/app seja importável pelo runtime do Vercel
current_dir = os.path.dirname(os.path.abspath(__file__))
root_dir = os.path.abspath(os.path.join(current_dir, ".."))
backend_dir = os.path.join(root_dir, "backend")

if backend_dir not in sys.path:
    sys.path.insert(0, backend_dir)

# Configuração e leitura do ambiente Supabase
if not os.environ.get("SUPABASE_URL"):
    os.environ["SUPABASE_URL"] = "https://jkreduqzekllsmzxiugn.supabase.co"
if not os.environ.get("ENVIRONMENT"):
    os.environ["ENVIRONMENT"] = "production"

from app.main import app
