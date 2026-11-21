/*
 * Lab 11: User Validation Web Application
 * -----------------------------------------
 * A login web application where the user submits a login name & password.
 * The credentials are validated against a MySQL database.
 * On success  → a welcome page is displayed.
 * On failure  → an error message is shown.
 *
 * Uses Java's built-in com.sun.net.httpserver.HttpServer (no Tomcat needed)
 * and JDBC to connect to MySQL.
 *
 * How to run:
 *   1. Make sure MySQL is running and execute setup_db.sql first.
 *   2. javac LoginServer.java
 *   3. java -cp ".;mysql-connector-j-8.0.33.jar" LoginServer
 *   4. Open http://localhost:8080 in your browser.
 */

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class LoginServer {

    // ── Database Configuration ──────────────────────────────────────────
    static final String DB_URL  = "jdbc:mysql://localhost:3306/user_login_db";
    static final String DB_USER = "root";
    static final String DB_PASS = "root";   // change to your MySQL root password

    public static void main(String[] args) throws Exception {
        // Load the JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Create HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Route: GET  /        → Login form
        // Route: POST /login   → Validate credentials
        server.createContext("/",      new LoginFormHandler());
        server.createContext("/login", new LoginHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("========================================");
        System.out.println("  Login Server started successfully!");
        System.out.println("  Open: http://localhost:8080");
        System.out.println("========================================");
    }

    // ─── Handler: Show the Login Form (GET /) ───────────────────────────
    static class LoginFormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                send(exchange, 404, errorPage("Page Not Found", "The requested page does not exist."));
                return;
            }

            String html = "<!DOCTYPE html>\n"
                + "<html lang='en'>\n"
                + "<head>\n"
                + "  <meta charset='UTF-8'>\n"
                + "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
                + "  <title>User Login</title>\n"
                + "  <style>\n"
                + CSS
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div class='container'>\n"
                + "    <div class='card'>\n"
                + "      <div class='icon'>&#128274;</div>\n"
                + "      <h1>User Login</h1>\n"
                + "      <p class='subtitle'>Enter your credentials to continue</p>\n"
                + "      <form method='POST' action='/login'>\n"
                + "        <div class='form-group'>\n"
                + "          <label for='username'>Login Name</label>\n"
                + "          <input type='text' id='username' name='username' placeholder='Enter your login name' required>\n"
                + "        </div>\n"
                + "        <div class='form-group'>\n"
                + "          <label for='password'>Password</label>\n"
                + "          <input type='password' id='password' name='password' placeholder='Enter your password' required>\n"
                + "        </div>\n"
                + "        <button type='submit' class='btn'>Login</button>\n"
                + "      </form>\n"
                + "      <p class='hint'>Demo accounts: admin/admin123, john/pass456, jane/secret789</p>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>";
            send(exchange, 200, html);
        }
    }

    // ─── Handler: Validate Login (POST /login) ──────────────────────────
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // If someone navigates to /login via GET, redirect to /
                exchange.getResponseHeaders().set("Location", "/");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            // Read form data from POST body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);

            String username = params.getOrDefault("username", "").trim();
            String password = params.getOrDefault("password", "").trim();

            if (username.isEmpty() || password.isEmpty()) {
                send(exchange, 400, errorPage("Missing Fields",
                        "Both login name and password are required."));
                return;
            }

            // Validate against the database
            boolean valid = validateUser(username, password);

            if (valid) {
                send(exchange, 200, successPage(username));
            } else {
                send(exchange, 401, failurePage());
            }
        }
    }

    // ─── Database Validation ────────────────────────────────────────────
    static boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // true if a matching row was found
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ─── HTML Page Generators ───────────────────────────────────────────

    static String successPage(String username) {
        return "<!DOCTYPE html>\n"
            + "<html lang='en'>\n"
            + "<head>\n"
            + "  <meta charset='UTF-8'>\n"
            + "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
            + "  <title>Login Successful</title>\n"
            + "  <style>\n" + CSS + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <div class='container'>\n"
            + "    <div class='card success'>\n"
            + "      <div class='icon'>&#9989;</div>\n"
            + "      <h1>Login Successful!</h1>\n"
            + "      <p class='welcome-msg'>Welcome, <strong>" + escapeHtml(username) + "</strong>!</p>\n"
            + "      <p>You have been successfully authenticated.</p>\n"
            + "      <p class='info'>Your credentials matched our database records.</p>\n"
            + "      <a href='/' class='btn'>Logout</a>\n"
            + "    </div>\n"
            + "  </div>\n"
            + "</body>\n"
            + "</html>";
    }

    static String failurePage() {
        return "<!DOCTYPE html>\n"
            + "<html lang='en'>\n"
            + "<head>\n"
            + "  <meta charset='UTF-8'>\n"
            + "  <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n"
            + "  <title>Login Failed</title>\n"
            + "  <style>\n" + CSS + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <div class='container'>\n"
            + "    <div class='card failure'>\n"
            + "      <div class='icon'>&#10060;</div>\n"
            + "      <h1>Login Failed!</h1>\n"
            + "      <p>Invalid login name or password.</p>\n"
            + "      <p class='info'>The credentials you entered do not match any records in our database.</p>\n"
            + "      <a href='/' class='btn'>Try Again</a>\n"
            + "    </div>\n"
            + "  </div>\n"
            + "</body>\n"
            + "</html>";
    }

    static String errorPage(String title, String message) {
        return "<!DOCTYPE html>\n"
            + "<html lang='en'>\n"
            + "<head>\n"
            + "  <meta charset='UTF-8'>\n"
            + "  <title>" + escapeHtml(title) + "</title>\n"
            + "  <style>\n" + CSS + "  </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "  <div class='container'>\n"
            + "    <div class='card failure'>\n"
            + "      <div class='icon'>&#9888;</div>\n"
            + "      <h1>" + escapeHtml(title) + "</h1>\n"
            + "      <p>" + escapeHtml(message) + "</p>\n"
            + "      <a href='/' class='btn'>Go Back</a>\n"
            + "    </div>\n"
            + "  </div>\n"
            + "</body>\n"
            + "</html>";
    }

    // ─── Utility Methods ────────────────────────────────────────────────

    static void send(HttpExchange exchange, int status, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static Map<String, String> parseFormData(String body) {
        Map<String, String> params = new HashMap<>();
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(
                    URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }

    static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ─── CSS Styles ─────────────────────────────────────────────────────
    static final String CSS = ""
        + "* { margin: 0; padding: 0; box-sizing: border-box; }\n"
        + "body {\n"
        + "  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n"
        + "  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n"
        + "  min-height: 100vh;\n"
        + "  display: flex;\n"
        + "  align-items: center;\n"
        + "  justify-content: center;\n"
        + "}\n"
        + ".container { width: 100%; max-width: 420px; padding: 20px; }\n"
        + ".card {\n"
        + "  background: #fff;\n"
        + "  border-radius: 16px;\n"
        + "  padding: 40px 35px;\n"
        + "  box-shadow: 0 15px 35px rgba(0,0,0,0.2);\n"
        + "  text-align: center;\n"
        + "}\n"
        + ".card.success { border-top: 5px solid #27ae60; }\n"
        + ".card.failure { border-top: 5px solid #e74c3c; }\n"
        + ".icon { font-size: 48px; margin-bottom: 10px; }\n"
        + "h1 {\n"
        + "  color: #333;\n"
        + "  margin-bottom: 8px;\n"
        + "  font-size: 24px;\n"
        + "}\n"
        + ".subtitle { color: #777; margin-bottom: 25px; font-size: 14px; }\n"
        + ".form-group {\n"
        + "  text-align: left;\n"
        + "  margin-bottom: 18px;\n"
        + "}\n"
        + "label {\n"
        + "  display: block;\n"
        + "  margin-bottom: 6px;\n"
        + "  color: #555;\n"
        + "  font-weight: 600;\n"
        + "  font-size: 14px;\n"
        + "}\n"
        + "input[type='text'], input[type='password'] {\n"
        + "  width: 100%;\n"
        + "  padding: 12px 15px;\n"
        + "  border: 2px solid #ddd;\n"
        + "  border-radius: 8px;\n"
        + "  font-size: 15px;\n"
        + "  transition: border-color 0.3s;\n"
        + "  outline: none;\n"
        + "}\n"
        + "input:focus { border-color: #667eea; }\n"
        + ".btn {\n"
        + "  display: inline-block;\n"
        + "  width: 100%;\n"
        + "  padding: 12px;\n"
        + "  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n"
        + "  color: #fff;\n"
        + "  border: none;\n"
        + "  border-radius: 8px;\n"
        + "  font-size: 16px;\n"
        + "  font-weight: 600;\n"
        + "  cursor: pointer;\n"
        + "  margin-top: 10px;\n"
        + "  text-decoration: none;\n"
        + "  text-align: center;\n"
        + "  transition: transform 0.2s;\n"
        + "}\n"
        + ".btn:hover { transform: translateY(-2px); }\n"
        + ".welcome-msg { font-size: 18px; color: #27ae60; margin: 15px 0; }\n"
        + ".info { color: #777; font-size: 13px; margin-top: 10px; }\n"
        + ".hint { color: #999; font-size: 12px; margin-top: 20px; }\n";
}
