import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private static final URI EMBEDDING_ENDPOINT = URI.create("http://localhost:11434/api/embed");
    private static final String DEFAULT_MODEL = "embeddinggemma";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
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
    public double[] createEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding text cannot be empty.");
        }

        ObjectNode requestBody = objectMapper.createObjectNode();

        requestBody.put("model", model);
        requestBody.put("input", text);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(EMBEDDING_ENDPOINT)
                .timeout(Duration.ofMinutes(5))
                .header(
                        "Content-Type",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                requestBody.toString()
                        )
                )
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new EmbeddingException("Ollama returned HTTP " + response.statusCode() + ": " + response.body());
            }

            return extractEmbedding(response.body());

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new EmbeddingException("The Ollama request was interrupted.", exception);

        } catch (IOException exception) {
            throw new EmbeddingException("Unable to communicate with Ollama. " + "Make sure Ollama is running.", exception);
        }
    }

    private double[] extractEmbedding(String responseBody) throws IOException {
        JsonNode responseJson = objectMapper.readTree(responseBody);
        JsonNode embeddingsNode = responseJson.get("embeddings");

        if (embeddingsNode == null || !embeddingsNode.isArray() || embeddingsNode.size() == 0) {
            throw new EmbeddingException("Ollama returned no embeddings.");
        }

        JsonNode vectorNode = embeddingsNode.get(0);
        if (!vectorNode.isArray() || vectorNode.size() == 0) {
            throw new EmbeddingException("Ollama returned an empty embedding.");
        }

        double[] embedding = new double[vectorNode.size()];
        for (int index = 0; index < vectorNode.size(); index++) {
            embedding[index] = vectorNode.get(index).asDouble();
        }

        return embedding;
    }
}