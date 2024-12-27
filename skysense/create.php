<?php
header("Content-Type: application/json");

// Koneksi database
$conn = new mysqli("localhost", "root", "", "skysense_db");
if ($conn->connect_error) die(json_encode(["success" => false, "message" => $conn->connect_error]));

// Ambil data POST
$name = $_POST['name'] ?? '';
$email = $_POST['email'] ?? '';
$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

if (!$name || !$email || !$username || !$password) {
    echo json_encode(["success" => false, "message" => "All fields are required"]);
    exit;
}

// Validasi username dan email
$checkQuery = $conn->prepare("SELECT id FROM users WHERE username = ? OR email = ?");
$checkQuery->bind_param("ss", $username, $email);
$checkQuery->execute();
if ($checkQuery->get_result()->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Username or email already exists"]);
    exit;
}

// Insert data
$insertQuery = $conn->prepare("INSERT INTO users (name, email, username, password) VALUES (?, ?, ?, ?)");
$insertQuery->bind_param("ssss", $name, $email, $username, $password);

if ($insertQuery->execute()) {
    echo json_encode(["success" => true, "message" => "User added successfully"]);
} else {
    echo json_encode(["success" => false, "message" => "Failed to add user"]);
}

$conn->close();
?>