package com.dando.repolens;

public class SearchResult {

    private final CodeChunk chunk;
    private final double score;

    public SearchResult(CodeChunk chunk, double score) {
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