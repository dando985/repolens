package com.dando.repolens;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/*
Builds a request containing the model and text.
Sends it to Ollama.
Checks for errors.
Extracts the returned numbers into a double[]
 */
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    // Connects to local Ollama endpoint (port 11434)
    private static final URI EMBEDDING_ENDPOINT = URI.create("http://localhost:11434/api/embed");
    // Define default Ollama embedding model
    private static final String DEFAULT_MODEL = "embeddinggemma";

    // Object to communicate with Ollama
    private final HttpClient httpClient;
    // Store the Jackson JSON utility
    private final ObjectMapper objectMapper;
    // Stores particular Ollama model used by provider
    private final String model;

    public OllamaEmbeddingProvider() {
        this(DEFAULT_MODEL);
    }

    public OllamaEmbeddingProvider(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("The embedding model cannot be empty.");
        }

        this.model = model;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public double[] createEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding text cannot be empty.");
        }

        // Create empty JSON object
        ObjectNode requestBody = objectMapper.createObjectNode();

        // Adds model and text to be embedded to JSON object
        requestBody.put("model", model);
        requestBody.put("input", text);

        // Build HTTP request object
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(EMBEDDING_ENDPOINT)
                .timeout(Duration.ofMinutes(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        // Send request to Ollama
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EmbeddingException("Ollama returned HTTP " + response.statusCode() + ": " + response.body());
            }

            // Extract the vector
            return extractEmbedding(response.body());
        } catch (InterruptedException exception) {
            // Clear thread's interrupted status
            Thread.currentThread().interrupt();
            throw new EmbeddingException("The Ollama request was interrupted.", exception);
        } catch (IOException exception) {
            throw new EmbeddingException("Unable to communicate with Ollama. " + "Make sure Ollama is running.", exception);
        }
    }

    // Helper to receive Ollama's JSON response as a string, extracts first embedding, and converts to double[]
    private double[] extractEmbedding(String responseBody) throws IOException {
        // Converts JSON text into a tree of JsonNode Objects
        JsonNode responseJson = objectMapper.readTree(responseBody);
        // Extract embeddings
        JsonNode embeddingsNode = responseJson.get("embeddings");

        if (embeddingsNode == null || !embeddingsNode.isArray() || embeddingsNode.size() == 0) {
            throw new EmbeddingException("Ollama returned no embeddings.");
        }

        JsonNode vectorNode = embeddingsNode.get(0);
        if (!vectorNode.isArray() || vectorNode.isEmpty()) {
            throw new EmbeddingException("Ollama returned an empty embedding.");
        }

        // Copies values from JsonNode object to a double[]
        double[] embedding = new double[vectorNode.size()];
        for (int index = 0; index < vectorNode.size(); index++) {
            embedding[index] = vectorNode.get(index).asDouble();
        }

        return embedding;
    }
}