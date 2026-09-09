package com.dando.repolens.model;

// Combines a CodeChunk with its corresponding relevance score in relation to a search query.
public class SearchResult {

    private final CodeChunk chunk;
    private final double score;

    public SearchResult(CodeChunk chunk, double score) {
        if (chunk == null) {
            throw new IllegalArgumentException("Code Chunk cannot be null");
        }
        if (!Double.isFinite(score)) {
            throw new IllegalArgumentException("Search score must be finite");
        }

        this.chunk = chunk;
        this.score = score;
    }

    public CodeChunk getChunk() {
        return chunk;
    }

    public double getScore() {
        return score;
    }
}