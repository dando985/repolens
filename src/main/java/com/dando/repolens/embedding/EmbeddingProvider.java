package com.dando.repolens.embedding;

public interface EmbeddingProvider {

    // Identifies embedding model
    String getModelName();

    // Converts text to embedding vector
    double[] createEmbedding(String text);
}