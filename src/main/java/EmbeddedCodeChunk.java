// Combines a code chunk with its corresponding embedding vector.
public class EmbeddedCodeChunk {

    private final CodeChunk chunk;
    private final double[] embedding;

    public EmbeddedCodeChunk(CodeChunk chunk, double[] embedding) {
        if (chunk == null) {
            throw new IllegalArgumentException("Code chunk cannot be null.");
        }

        if (embedding == null || embedding.length == 0) {
            throw new IllegalArgumentException("Embedding cannot be empty.");
        }

        this.chunk = chunk;
        this.embedding = embedding.clone();
    }

    public CodeChunk getChunk() {
        return chunk;
    }

    public double[] getEmbedding() {
        return embedding.clone();
    }
}