package com.dando.repolens.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.time.Duration;

/*
Builds a request containing the model and text.
Sends it to Ollama.
Checks for errors.
Extracts the returned numbers into a double[]
 */
public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI embedEndpoint;
    private final String modelName;

    public OllamaEmbeddingProvider(String baseUrl, String modelName) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("The Ollama base URL cannot be blank.");
        }

        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("The Ollama model name cannot be blank.");
        }

        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.embedEndpoint = URI.create(normalizedBaseUrl + "/api/embed");

        this.modelName = modelName.trim();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();

    }

    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public double[] createEmbedding(String text) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Embedding text cannot be null or blank."
            );
        }

        try {
            // Build the request body as a JSON string
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", modelName,
                    "input", text));


            HttpRequest request = HttpRequest.newBuilder()
                    .uri(embedEndpoint)
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send HTTP request and wait for the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EmbeddingException("Ollama returned HTTP status " + response.statusCode() + ": " + response.body());
            }

            // Parse the response body as JSON and extract the embedding
            JsonNode responseJson = objectMapper.readTree(response.body());
            JsonNode embeddingsNode = responseJson.path("embeddings");

            if (!embeddingsNode.isArray() || embeddingsNode.isEmpty()) {
                throw new EmbeddingException("Ollama response did not contain an embedding");
            }

            JsonNode vectorNode = embeddingsNode.get(0);
            double[] embedding = new double[vectorNode.size()];

            for (int index = 0; index < vectorNode.size(); index++) {
                embedding[index] = vectorNode.get(index).asDouble();
            }

            return embedding;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new EmbeddingException("Ollama request was interrupted", exception);
        } catch (IOException exception) {
            throw new EmbeddingException("Unable to communicate with Ollama", exception);
        }
    }
}