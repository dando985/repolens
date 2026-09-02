import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A utility class for saving and loading a semantic index of code chunks to and from a JSON file.
 * The semantic index is associated with a specific embedding model and contains a list of embedded code chunks,
 * each with its corresponding embedding vector. The class provides methods to save the index to a file and load it
 * if it is valid (i.e., matches the current model and code chunks).
  */
public class SemanticIndexStore {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Saves the semantic index to a JSON file at the specified path. The index is associated with a specific embedding model and contains a list of embedded code chunks.
    public void save(Path indexPath, String modelName, List<EmbeddedCodeChunk> semanticIndex) throws IOException {

        ObjectNode root = objectMapper.createObjectNode();

        // Store the model name in the root JSON object
        root.put("model", modelName);
        // Create an array node to hold the embedded code chunks
        ArrayNode chunksNode = root.putArray("chunks");

        // Iterate over the list of embedded code chunks and add their properties to the JSON array (chunksNode)
        for (EmbeddedCodeChunk embeddedChunk : semanticIndex) {
            // Create entry to populate with the properties of the code chunk and its embedding vector
            ObjectNode chunkNode = chunksNode.addObject();

            // Store the properties of the code chunk in the JSON object
            CodeChunk chunk = embeddedChunk.getChunk();
            chunkNode.put("filePath", chunk.getFilePath().toString());
            chunkNode.put("className", chunk.getClassName());
            chunkNode.put("methodName", chunk.getMethodName());
            chunkNode.put("startLine", chunk.getStartLine());
            chunkNode.put("endLine", chunk.getEndLine());
            chunkNode.put("content", chunk.getContent());

            // Create an array node to hold the embedding vector for the code chunk
            ArrayNode embeddingNode = chunkNode.putArray("embedding");
            for (double value : embeddedChunk.getEmbedding()) {
                embeddingNode.add(value);
            }
        }

        // Create the parent directory for the index file if it doesn't exist
        Path parentDirectory = indexPath.getParent();
        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        // Write the JSON representation of the semantic index to the specified file with pretty printing
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexPath.toFile(), root);
    }

    /**
     * Loads the semantic index from a JSON file at the specified path if it is valid.
     * The index is considered valid if it matches the current embedding model and code chunks.
     */
    public Optional<List<EmbeddedCodeChunk>> loadIfValid(Path indexPath, String modelName, List<CodeChunk> currentChunks) {
        if (!Files.exists(indexPath)) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(indexPath.toFile());

            // Check if the stored model name matches the current model name
            String storedModel = root.path("model").asText();
            if (!modelName.equals(storedModel)) {
                return Optional.empty();
            }

            // Check if the stored chunks match the current chunks in terms of count and content
            JsonNode storedChunksNode = root.path("chunks");
            if (!storedChunksNode.isArray() || storedChunksNode.size() != currentChunks.size()) {
                return Optional.empty();
            }

            // Create a map to store the stored chunks for quick lookup by their unique keys
            Map<String, JsonNode> storedChunks = new HashMap<>();
            for (JsonNode storedChunk : storedChunksNode) {
                // Create a unique key for the stored chunk based on its properties
                String key = createStoredChunkKey(storedChunk);

                if (storedChunks.put(key, storedChunk) != null) {
                    return Optional.empty();
                }
            }

            List<EmbeddedCodeChunk> semanticIndex = new ArrayList<>();
            for (CodeChunk currentChunk : currentChunks) {
                // Create a unique key for the current code chunk based on its properties
                String key = createChunkKey(currentChunk);

                // Retrieve the corresponding stored chunk from the map using the unique key. If it doesn't exist, return empty.
                JsonNode storedChunk = storedChunks.remove(key);
                if (storedChunk == null) {
                    return Optional.empty();
                }

                // Compare the content of the current code chunk with the stored content. If they don't match, return empty.
                String storedContent = storedChunk.path("content").asText();
                if (!currentChunk.getContent().equals(storedContent)) {
                    return Optional.empty();
                }

                // Read the embedding vector from the stored chunk and create an EmbeddedCodeChunk object to add to the semantic index
                double[] embedding = readEmbedding(storedChunk);
                semanticIndex.add(new EmbeddedCodeChunk(currentChunk, embedding));
            }

            // After processing all current chunks, check if there are any remaining stored chunks in the map.
            // If there are, it means there are extra stored chunks that don't match the current chunks, so return empty.
            if (!storedChunks.isEmpty()) {
                return Optional.empty();
            }

            // If all checks pass, return the semantic index
            return Optional.of(semanticIndex);

        } catch (IOException | RuntimeException exception) {
            return Optional.empty();
        }
    }

    // Reads the embedding vector from a stored chunk represented as a JsonNode. The embedding is expected to be an array of doubles.
    private double[] readEmbedding(JsonNode storedChunk) {
        JsonNode embeddingNode = storedChunk.path("embedding");

        if (!embeddingNode.isArray() || embeddingNode.size() == 0) {
            throw new IllegalArgumentException("Stored embedding is invalid.");
        }

        // Convert the embedding JsonNode array to a double array
        double[] embedding = new double[embeddingNode.size()];
        for (int index = 0; index < embeddingNode.size(); index++) {
            embedding[index] = embeddingNode.get(index).asDouble();
        }

        return embedding;
    }

    // Wrapper method to create a unique key for a code chunk object based on its properties.
    private String createChunkKey(CodeChunk chunk) {
        return createKey(chunk.getFilePath().toString(),
                chunk.getClassName(),
                chunk.getMethodName(),
                chunk.getStartLine(),
                chunk.getEndLine());
    }

    // Wrapper method to create a unique key for a stored code chunk (JsonNode object) based on its properties.
    private String createStoredChunkKey(JsonNode storedChunk) {
        return createKey(storedChunk.path("filePath").asText(),
                storedChunk.path("className").asText(),
                storedChunk.path("methodName").asText(),
                storedChunk.path("startLine").asInt(),
                storedChunk.path("endLine").asInt());
    }

    // Creates a custom key for a code chunk
    private String createKey(String filePath, String className, String methodName, int startLine, int endLine) {
        return filePath + "|" + className + "|" + methodName + "|" + startLine + "|" + endLine;
    }
}