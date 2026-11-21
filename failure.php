<?php
$user = isset($_GET['user']) ? htmlspecialchars($_GET['user']) : 'user';
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login Failed - Lab 11</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="container">
        <div class="status-card failure">
            <div class="status-icon">&#10060;</div>
            <div class="status-title">Login Failed!</div>
            <p>Invalid login name or password provided for: <code><?php echo $user; ?></code></p>
        </div>

        <div class="info-box">
            <p><strong>Reason:</strong> The credentials submitted did not match any record in the server database.</p>
            <p><strong>Action:</strong> Please check your username and password, then try again.</p>
        </div>

        <a href="index.html" class="btn">Try Again</a>
    </div>
</body>
</html>
