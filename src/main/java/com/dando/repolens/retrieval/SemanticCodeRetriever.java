package com.dando.repolens.retrieval;

import com.dando.repolens.embedding.EmbeddingProvider;
import com.dando.repolens.model.EmbeddedCodeChunk;
import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// A code retriever that uses semantic similarity to find relevant code chunks for a given search query.
// It uses an embedding provider to create embeddings for the search query and code chunks, and calculates the cosine similarity between them to rank the results.
public class SemanticCodeRetriever implements CodeRetriever {

    private final EmbeddingProvider embeddingProvider;
    private final List<EmbeddedCodeChunk> embeddedChunks;

    public SemanticCodeRetriever(EmbeddingProvider embeddingProvider, List<EmbeddedCodeChunk> embeddedChunks) {
        this.embeddingProvider = Objects.requireNonNull(embeddingProvider);
        this.embeddedChunks = Objects.requireNonNull(embeddedChunks);
    }

    @Override
    public List<SearchResult> search(SearchQuery searchQuery) {
        if (embeddedChunks.isEmpty()) {
            return new ArrayList<>();
        }

        // Create embedding for the search query
        double[] queryEmbedding = embeddingProvider.createEmbedding(searchQuery.getText());

        // Calculate similarity score for each code chunk and add to results
        List<SearchResult> results = new ArrayList<>();
        for (EmbeddedCodeChunk embeddedChunk : embeddedChunks) {
            // Calculate cosine similarity between the query embedding and the chunk embedding
            double similarity = VectorSimilarity.cosineSimilarity(queryEmbedding, embeddedChunk.getEmbedding());

            // Add the code chunk and its similarity score to the results list
            results.add(new SearchResult(embeddedChunk.getChunk(), similarity));
        }

        // Sort results in descending order of similarity score
        results.sort((first, second) -> Double.compare(second.getScore(), first.getScore()));

        // Limit the number of results to the maximum specified in the search query
        int resultCount = Math.min(searchQuery.getMaxResults(), results.size());
        return new ArrayList<>(results.subList(0, resultCount));
    }
}