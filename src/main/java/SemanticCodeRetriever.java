import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// A code retriever that uses semantic similarity to find relevant code chunks for a given search query.
// It uses an embedding provider to create embeddings for the search query and code chunks, and calculates the cosine similarity between them to rank the results.
public class SemanticCodeRetriever implements CodeRetriever {

    private final EmbeddingProvider embeddingProvider;

    public SemanticCodeRetriever(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = Objects.requireNonNull(embeddingProvider);
    }

    @Override
    public List<SearchResult> search(List<CodeChunk> chunks, SearchQuery searchQuery) {
        if (chunks.isEmpty()) {
            return new ArrayList<>();
        }

        // Create embedding for the search query
        double[] queryEmbedding = embeddingProvider.createEmbedding(searchQuery.getText());

        // Calculate similarity score for each code chunk and add to results
        List<SearchResult> results = new ArrayList<>();
        for (CodeChunk chunk : chunks) {
            // Create embedding text for the code chunk
            String chunkText = createEmbeddingText(chunk);

            // Create embedding for the code chunk
            double[] chunkEmbedding = embeddingProvider.createEmbedding(chunkText);

            // Calculate cosine similarity between the query embedding and the chunk embedding
            double similarity = VectorSimilarity.cosineSimilarity(queryEmbedding, chunkEmbedding);

            // Add the code chunk and its similarity score to the results list
            results.add(new SearchResult(chunk, similarity));
        }

        // Sort results in descending order of similarity score
        results.sort((first, second) -> Double.compare(second.getScore(), first.getScore()));

        // Limit the number of results to the maximum specified in the search query
        int resultCount = Math.min(searchQuery.getMaxResults(), results.size());
        return new ArrayList<>(results.subList(0, resultCount));
    }

    // Creates a string representation of a code chunk's metadata and content for embedding purposes
    private String createEmbeddingText(CodeChunk chunk) {
        return "Class: " + chunk.getClassName() + System.lineSeparator()
                + "Method: " + chunk.getMethodName() + System.lineSeparator()
                + "Code:" + System.lineSeparator() + chunk.getContent();
    }
}