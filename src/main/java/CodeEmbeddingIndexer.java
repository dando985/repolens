import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Sends code chunks to the embedding provider to create embeddings and build an index of embedded code chunks
public class CodeEmbeddingIndexer {

    private final EmbeddingProvider embeddingProvider;

    public CodeEmbeddingIndexer(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = Objects.requireNonNull(embeddingProvider);
    }

    public List<EmbeddedCodeChunk> createIndex(List<CodeChunk> chunks) {
        List<EmbeddedCodeChunk> index = new ArrayList<>();

        for (CodeChunk chunk : chunks) {
            String embeddingText = createEmbeddingText(chunk);

            double[] embedding = embeddingProvider.createEmbedding(embeddingText);
            index.add(new EmbeddedCodeChunk(chunk, embedding));
        }

        return index;
    }

    // Creates a text representation of a code chunk and its metadata to be used for embedding generation
    private String createEmbeddingText(CodeChunk chunk) {
        return "File: " + chunk.getFilePath().getFileName() + System.lineSeparator()
                + "Class: " + chunk.getClassName() + System.lineSeparator()
                + "Method: " + chunk.getMethodName() + System.lineSeparator()
                + "Code:" + System.lineSeparator() + chunk.getContent();
    }
}