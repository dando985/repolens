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

public class SemanticIndexStore {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void save(Path indexPath, String modelName, List<EmbeddedCodeChunk> semanticIndex) throws IOException {

        ObjectNode root = objectMapper.createObjectNode();

        root.put("model", modelName);

        ArrayNode chunksNode = root.putArray("chunks");

        for (EmbeddedCodeChunk embeddedChunk : semanticIndex) {

            CodeChunk chunk = embeddedChunk.getChunk();

            ObjectNode chunkNode = chunksNode.addObject();
            chunkNode.put("filePath", chunk.getFilePath().toString());
            chunkNode.put("className", chunk.getClassName());
            chunkNode.put("methodName", chunk.getMethodName());
            chunkNode.put("startLine", chunk.getStartLine());
            chunkNode.put("endLine", chunk.getEndLine());
            chunkNode.put("content", chunk.getContent());

            ArrayNode embeddingNode = chunkNode.putArray("embedding");

            for (double value : embeddedChunk.getEmbedding()) {
                embeddingNode.add(value);
            }
        }

        Path parentDirectory = indexPath.getParent();

        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(indexPath.toFile(), root);
    }

    public Optional<List<EmbeddedCodeChunk>> loadIfValid(Path indexPath, String modelName, List<CodeChunk> currentChunks) {
        if (!Files.exists(indexPath)) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(indexPath.toFile());

            String storedModel = root.path("model").asText();

            if (!modelName.equals(storedModel)) {
                return Optional.empty();
            }

            JsonNode storedChunksNode = root.path("chunks");

            if (!storedChunksNode.isArray() || storedChunksNode.size() != currentChunks.size()) {
                return Optional.empty();
            }

            Map<String, JsonNode> storedChunks = new HashMap<>();

            for (JsonNode storedChunk : storedChunksNode) {
                String key = createStoredChunkKey(storedChunk);

                if (storedChunks.put(key, storedChunk) != null) {
                    return Optional.empty();
                }
            }

            List<EmbeddedCodeChunk> semanticIndex = new ArrayList<>();

            for (CodeChunk currentChunk : currentChunks) {
                String key = createChunkKey(currentChunk);
                JsonNode storedChunk = storedChunks.remove(key);

                if (storedChunk == null) {
                    return Optional.empty();
                }

                String storedContent = storedChunk.path("content").asText();

                if (!currentChunk.getContent().equals(storedContent)) {
                    return Optional.empty();
                }

                double[] embedding = readEmbedding(storedChunk);

                semanticIndex.add(new EmbeddedCodeChunk(currentChunk, embedding));
            }

            if (!storedChunks.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(semanticIndex);

        } catch (IOException | RuntimeException exception) {

            return Optional.empty();
        }
    }

    private double[] readEmbedding(JsonNode storedChunk) {
        JsonNode embeddingNode = storedChunk.path("embedding");

        if (!embeddingNode.isArray() || embeddingNode.size() == 0) {
            throw new IllegalArgumentException("Stored embedding is invalid.");
        }

        double[] embedding = new double[embeddingNode.size()];

        for (int index = 0; index < embeddingNode.size(); index++) {
            embedding[index] = embeddingNode.get(index).asDouble();
        }

        return embedding;
    }

    private String createChunkKey(CodeChunk chunk) {
        return createKey(chunk.getFilePath().toString(), chunk.getClassName(), chunk.getMethodName(), chunk.getStartLine(), chunk.getEndLine());
    }

    private String createStoredChunkKey(JsonNode storedChunk) {
        return createKey(storedChunk.path("filePath").asText(),

                storedChunk.path("className").asText(),
                storedChunk.path("methodName").asText(),
                storedChunk.path("startLine").asInt(),
                storedChunk.path("endLine").asInt());
    }

    private String createKey(String filePath, String className, String methodName, int startLine, int endLine) {
        return filePath + "|" + className + "|" + methodName + "|" + startLine + "|" + endLine;
    }
}