<?php
header("Content-Type: application/json");

$host = 'localhost';
$db_name = 'skysense_db';
$db_user = 'root';
$db_password = '';

// Koneksi database
$conn = new mysqli($host, $db_user, $db_password, $db_name);

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Database connection failed: " . $conn->connect_error]);
    exit();
}

// Ambil data dari POST request
$username = $_POST['username'] ?? null;

if (!$username) {
    echo json_encode(["success" => false, "message" => "Username is required"]);
    exit();
}

// Hapus user dari database
$query = "DELETE FROM users WHERE username = ?";
$stmt = $conn->prepare($query);
$stmt->bind_param("s", $username);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "User deleted successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Failed to delete user"]);
}

$stmt->close();
$conn->close();
?>
