<?php
// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 1);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $userMessage = $_POST['message'] ?? '';

    if (empty($userMessage)) {
        echo json_encode(["error" => "Message is empty!"]);
        exit;
    }

    // JAX-RS API URL
    $apiUrl = 'http://localhost:8080/Darija-backend-1.0-SNAPSHOT/api/translator/translate';

    // Send request to the JAX-RS API
    $response = @file_get_contents($apiUrl, false, stream_context_create([
        'http' => [
            'method'  => 'POST',
            'header'  => "Content-Type: text/plain\r\n",
            'content' => $userMessage,
        ]
    ]));

    if ($response === false) {
        $error = error_get_last();
        echo json_encode(["error" => "Unable to connect to API.", "details" => $error['message']]);
    } else {
        // Decode the JSON response from the JAX-RS API
        $decodedResponse = json_decode($response, true);

        if (json_last_error() === JSON_ERROR_NONE) {
            // Return the translated text to the frontend
            echo json_encode([
                "original" => $decodedResponse['original'] ?? $userMessage,
                "translated" => $decodedResponse['translated'] ?? "Translation not available."
            ]);
        } else {
            echo json_encode(["error" => "Invalid response from API.", "response" => $response]);
        }
    }
}
?>