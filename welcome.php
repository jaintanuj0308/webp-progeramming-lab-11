<?php
session_start();
if (!isset($_SESSION['username'])) {
    header("Location: index.html");
    exit();
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Successful - Lab 11</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container">
        <div class="status-card success">
            <div class="status-icon">&#9989;</div>
            <div class="status-title">Login Successful!</div>
            <p>Welcome back, <strong><?php echo htmlspecialchars($_SESSION['fullname']); ?></strong> (<code><?php echo htmlspecialchars($_SESSION['username']); ?></code>)</p>
        </div>

        <div class="info-box">
            <p><strong>Database Match:</strong> User credentials verified against Database records.</p>
            <p><strong>Account Role:</strong> <?php echo htmlspecialchars($_SESSION['role']); ?></p>
            <p><strong>Authentication Time:</strong> <?php echo htmlspecialchars($_SESSION['login_time']); ?></p>
            <p><strong>Status:</strong> Active Session Granted</p>
        </div>

        <a href="logout.php" class="btn btn-logout">Logout / Back to Login</a>
    </div>
</body>
</html>
