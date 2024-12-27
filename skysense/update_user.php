<?php

header('Content-Type: application/json');

// Koneksi ke database
$host = "localhost";
$dbname = "skysense_db";
$username = "root";
$password = "";

// Gunakan PDO untuk koneksi yang lebih aman
try {
    $conn = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    die(json_encode([
        "success" => false,
        "message" => "Connection failed: " . $e->getMessage()
    ]));
}

// Validasi input
if (!isset($_POST['current_username']) || !isset($_POST['name']) || !isset($_POST['email']) || !isset($_POST['username'])) {
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields."
    ]);
    exit;
}

// Ambil data dari request
$current_username = $_POST['current_username'];
$new_name = $_POST['name'];
$new_email = $_POST['email'];
$new_username = $_POST['username'];
$new_password = isset($_POST['password']) ? $_POST['password'] : '';

try {
    // Cek apakah username baru sudah ada (kecuali username saat ini)
    $stmt = $conn->prepare("SELECT id FROM users WHERE username = :username AND username != :current_username");
    $stmt->bindParam(':username', $new_username);
    $stmt->bindParam(':current_username', $current_username);
    $stmt->execute();
    
    if ($stmt->rowCount() > 0) {
        echo json_encode([
            "success" => false,
            "message" => "Username already exists."
        ]);
        exit;
    }
    
    // Update user data
    if (!empty($new_password)) {
        // Update dengan password baru tanpa hashing
        $stmt = $conn->prepare("UPDATE users SET name = :name, email = :email, username = :username, password = :password WHERE username = :current_username");
        $stmt->bindParam(':password', $new_password);
    } else {
        // Update tanpa password
        $stmt = $conn->prepare("UPDATE users SET name = :name, email = :email, username = :username WHERE username = :current_username");
    }
    
    $stmt->bindParam(':name', $new_name);
    $stmt->bindParam(':email', $new_email);
    $stmt->bindParam(':username', $new_username);
    $stmt->bindParam(':current_username', $current_username);
    
    if ($stmt->execute()) {
        echo json_encode([
            "success" => true,
            "message" => "Profile updated successfully."
        ]);
    } else {
        echo json_encode([
            "success" => false,
            "message" => "Error updating profile."
        ]);
    }
    
} catch(PDOException $e) {
    echo json_encode([
        "success" => false,
        "message" => "Database error: " . $e->getMessage()
    ]);
}

$conn = null; // Close connection
?>
