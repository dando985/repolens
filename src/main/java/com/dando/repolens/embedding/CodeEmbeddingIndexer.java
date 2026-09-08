package com.dando.repolens.embedding;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Sends code chunks to the embedding provider to create embeddings and build an index of embedded code chunks
@Service
public class CodeEmbeddingIndexer {

    private final EmbeddingProvider embeddingProvider;

    public CodeEmbeddingIndexer(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
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