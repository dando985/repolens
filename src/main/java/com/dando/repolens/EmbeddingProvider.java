package com.dando.repolens;

public interface EmbeddingProvider {

    // Needed to avoid mixing embeddings from different models in the same index file.
    // The model name is stored in the index file and checked when loading the index.
    String getModelName();

    double[] createEmbedding(String text);
}