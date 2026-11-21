"""
Lab 11: User Validation Web Application
----------------------------------------
This server handles user login submissions, checks credentials against
the SQLite database (`users.db`), and returns:
  - A Successful Login Page if credentials match
  - A Failure Message Page if credentials do not match

Runs with zero dependencies using Python's built-in `http.server` & `sqlite3`.
"""

import http.server
import socketserver
import urllib.parse
import sqlite3
import os
import datetime

PORT = 8000
DB_FILE = os.path.join(os.path.dirname(__file__), "users.db")


def init_database():
    """Initializes the database and populates sample users if not present."""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            fullname TEXT NOT NULL,
            role TEXT NOT NULL
        )
    """)
    # Insert initial sample users if empty
    cursor.execute("SELECT COUNT(*) FROM users")
    if cursor.fetchone()[0] == 0:
        sample_users = [
            ("admin", "admin123", "System Administrator", "Administrator"),
            ("john", "pass456", "John Doe", "Student"),
            ("alice", "secret789", "Alice Johnson", "Faculty")
        ]
        cursor.executemany("INSERT INTO users (username, password, fullname, role) VALUES (?, ?, ?, ?)", sample_users)
        conn.commit()
    conn.close()


def validate_user(username, password):
    """Checks the submitted credentials against the database."""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute("SELECT fullname, role FROM users WHERE username = ? AND password = ?", (username, password))
    user = cursor.fetchone()
    conn.close()
    return user


class LoginHandler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path in ("/", "/index.html"):
            self.serve_file("index.html", "text/html; charset=utf-8")
        elif self.path == "/style.css":
            self.serve_file("style.css", "text/css; charset=utf-8")
        else:
            self.send_error(404, "File Not Found")

    def do_POST(self):
        if self.path == "/login":
            content_length = int(self.headers.get("Content-Length", 0))
            post_data = self.rfile.read(content_length).decode("utf-8")
            params = urllib.parse.parse_qs(post_data)

            username = params.get("username", [""])[0].strip()
            password = params.get("password", [""])[0].strip()

            # Validate against database
            user_record = validate_user(username, password)

            if user_record:
                fullname, role = user_record
                self.send_success_page(username, fullname, role)
            else:
                self.send_failure_page(username)
        else:
            self.send_error(404, "Not Found")

    def serve_file(self, filename, content_type):
        filepath = os.path.join(os.path.dirname(__file__), filename)
        if os.path.exists(filepath):
            self.send_response(200)
            self.send_header("Content-Type", content_type)
            self.end_headers()
            with open(filepath, "rb") as f:
                self.wfile.write(f.read())
        else:
            self.send_error(404, "File Not Found")

    def send_success_page(self, username, fullname, role):
        now_str = datetime.datetime.now().strftime("%d %B %Y, %I:%M:%S %p")
        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Successful - Lab 11</title>
    <link rel="stylesheet" href="/style.css">
</head>
<body>
    <div class="container">
        <div class="status-card success">
            <div class="status-icon">&#9989;</div>
            <div class="status-title">Login Successful!</div>
            <p>Welcome back, <strong>{fullname}</strong> (<code>{username}</code>)</p>
        </div>

        <div class="info-box">
            <p><strong>Database Match:</strong> User credentials verified against Database records.</p>
            <p><strong>Account Role:</strong> {role}</p>
            <p><strong>Authentication Time:</strong> {now_str}</p>
            <p><strong>Status:</strong> Active Session Granted</p>
        </div>

        <a href="/" class="btn btn-logout">Logout / Back to Login</a>
    </div>
</body>
</html>"""
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()
        self.wfile.write(html.encode("utf-8"))

    def send_failure_page(self, username):
        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Failed - Lab 11</title>
    <link rel="stylesheet" href="/style.css">
</head>
<body>
    <div class="container">
        <div class="status-card failure">
            <div class="status-icon">&#10060;</div>
            <div class="status-title">Login Failed!</div>
            <p>Invalid login name or password provided for: <code>{username if username else "(blank)"}</code></p>
        </div>

        <div class="info-box">
            <p><strong>Reason:</strong> The credentials submitted did not match any record in the server database.</p>
            <p><strong>Action:</strong> Please check your username and password, then try again.</p>
        </div>

        <a href="/" class="btn">Try Again</a>
    </div>
</body>
</html>"""
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()
        self.wfile.write(html.encode("utf-8"))


if __name__ == "__main__":
    init_database()
    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), LoginHandler) as httpd:
        print(f"==================================================")
        print(f" Lab 11 User Validation Server running!")
        print(f" URL: http://localhost:{PORT}")
        print(f" Database: {DB_FILE} (SQLite)")
        print(f"==================================================")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nServer stopped.")
