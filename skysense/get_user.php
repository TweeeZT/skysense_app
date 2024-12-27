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

// Ambil username dari request
if (isset($_POST['username'])) {
    $user = $_POST['username'];
    
    try {
        // Gunakan prepared statement untuk mencegah SQL injection
        $stmt = $conn->prepare("SELECT id, name, email, username FROM users WHERE username = :username LIMIT 1");
        $stmt->bindParam(':username', $user);
        $stmt->execute();
        
        if ($stmt->rowCount() > 0) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            // Jangan kirim password dalam response
            echo json_encode([
                "success" => true,
                "user" => $row
            ]);
        } else {
            echo json_encode([
                "success" => false,
                "message" => "User not found."
            ]);
        }
    } catch(PDOException $e) {
        echo json_encode([
            "success" => false,
            "message" => "Database error: " . $e->getMessage()
        ]);
    }
} else {
    echo json_encode([
        "success" => false,
        "message" => "Username is required."
    ]);
}

$conn = null; // Close connection
?>