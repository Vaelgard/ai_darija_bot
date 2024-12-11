package ma.darija.app.darijabackend;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.JSONObject;

@Path("/translator")
public class TranslatorResource {

    private static final Properties CONFIG = loadConfig();

    private static final String GEMINI_API_KEY = CONFIG.getProperty("GEMINI_API_KEY");
    private static final String GEMINI_ENDPOINT = CONFIG.getProperty("GEMINI_ENDPOINT");

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
            String translation = callGeminiAPI(englishText);

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
        URL url = new URL(GEMINI_ENDPOINT + GEMINI_API_KEY);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        String payload = "{\n" +
                "  \"system_instruction\": {\n" +
                "    \"parts\": [\n" +
                "      { \"text\": \"Translate the following text into Moroccan Darija, a colloquial Arabic dialect commonly spoken in Morocco. Provide only the best and most natural translation as a single response. Give just the raw answer.\" }\n" +
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

        connection.getOutputStream().write(payload.getBytes());

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

        System.out.println("API Response: " + response);

        JSONObject jsonResponse = new JSONObject(response.toString());
        return jsonResponse.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text");
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        try (InputStream input = TranslatorResource.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("Configuration file not found.");
            }
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration file.", e);
        }
        return properties;
    }
}
