<?php
// Database configuration
$host = "localhost";
$db_user = "root";
$db_pass = ""; // update with your MySQL root password if set
$db_name = "lab11_db";

// Connect to MySQL server
$conn = new mysqli($host, $db_user, $db_pass, $db_name);

// Check connection
if ($conn->connect_error) {
    die("Database Connection failed: " . $conn->connect_error);
}

// Receive form submission
if ($_SERVER["REQUEST_METHOD"] === "POST") {
    $username = isset($_POST['username']) ? trim($_POST['username']) : '';
    $password = isset($_POST['password']) ? trim($_POST['password']) : '';

    if (empty($username) || empty($password)) {
        header("Location: failure.php?error=empty");
        exit();
    }

    // Prepare statement to avoid SQL injection
    $stmt = $conn->prepare("SELECT fullname, role FROM users WHERE username = ? AND password = ?");
    $stmt->bind_param("ss", $username, $password);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        // Successful login
        session_start();
        $_SESSION['username'] = $username;
        $_SESSION['fullname'] = $row['fullname'];
        $_SESSION['role'] = $row['role'];
        $_SESSION['login_time'] = date('d F Y, h:i:s A');
        header("Location: welcome.php");
        exit();
    } else {
        // Failed login
        header("Location: failure.php?user=" . urlencode($username));
        exit();
    }

    $stmt->close();
}

$conn->close();
?>
