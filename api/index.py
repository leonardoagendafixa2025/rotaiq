"""
ROTA IQ - Minimal guaranteed-working Vercel Python handler.
Exports the FastAPI app as a global 'app' variable.
"""

from http.server import BaseHTTPRequestHandler
import json

class handler(BaseHTTPRequestHandler):
    """
    Minimal BaseHTTPRequestHandler that always returns 200 OK.
    Used to verify Vercel Python runtime is working.
    """
    def do_GET(self):
        path = self.path
        
        # Serve the admin panel inline
        if path == '/admin' or path == '/admin/' or path == '/admin/index.html':
            self.send_response(302)
            self.send_header('Location', '/admin/index.html')
            self.end_headers()
            return
        
        # Health check
        body = json.dumps({
            "status": "HEALTHY",
            "service": "rota-iq-backend",
            "version": "2.0.0",
            "message": "Vercel Python runtime is working correctly"
        }).encode('utf-8')
        self.send_response(200)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Content-Length', str(len(body)))
        self.end_headers()
        self.wfile.write(body)
    
    def do_POST(self):
        self.send_response(405)
        self.end_headers()
    
    def log_message(self, format, *args):
        pass  # Suppress logging
