package ma.darija.app.darijabackend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;

@Path("/translator")
public class TranslatorResource {

    private static final String GEMINI_API_KEY = "AIzaSyAhGhOXiwtiNRjrLlf6h3wQ8j38j4iG1PE"; // Replace with your actual key
    private static final String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";

    @POST
    @Path("/translate")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response translate(String englishText) {
        if (englishText == null || englishText.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Input text cannot be empty.\"}")
                    .build();
        }

        try {
            // Call the Gemini API to translate from English to Moroccan Darija
            String translation = callGeminiAPI(englishText);

            // Return the response with the original and translated text
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("original", englishText);
            jsonResponse.put("translated", translation);
            return Response.ok(jsonResponse.toString()).build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to process translation.\"}")
                    .build();
        }
    }

    private String callGeminiAPI(String text) throws IOException {
        // Construct the API request URL
        URL url = new URL(GEMINI_ENDPOINT + GEMINI_API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Prepare the payload with improved instruction for translating to Moroccan Darija
        String payload = "{\n" +
                "  \"system_instruction\": {\n" +
                "    \"parts\": [\n" +
                "      { \"text\": \"Translate the following text into Moroccan Darija, a colloquial Arabic dialect commonly spoken in Morocco. Provide only the best and most natural translation as a single response.\" }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        { \"text\": \"" + text + "\" }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Write payload to the connection
        connection.getOutputStream().write(payload.getBytes());

        // Read the API response
        Scanner scanner;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            scanner = new Scanner(connection.getInputStream());
        } else {
            scanner = new Scanner(connection.getErrorStream());
        }

        StringBuilder response = new StringBuilder();
        while (scanner.hasNextLine()) {
            response.append(scanner.nextLine());
        }

        scanner.close();
        connection.disconnect();

        // Print the raw API response for debugging purposes
        System.out.println("API Response: " + response);

        // Parse the JSON response to extract the translated text
        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getJSONArray("candidates")
                .getJSONObject(0) // Get the first candidate
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
    }
}
