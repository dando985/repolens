package com.dando.repolens.service;

import com.dando.repolens.embedding.CodeEmbeddingIndexer;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticIndexServiceTest {

    @Mock
    RepositoryAnalysisService analysisService;

    @Mock
    CodeEmbeddingIndexer embeddingIndexer;

    @InjectMocks
    SemanticIndexService semanticIndexService;

    @TempDir
    Path repositoryPath;

    @Test
    void reusesIndexForTheSameRepository() throws IOException {
        CodeChunk chunk = createCodeChunk();
        List<CodeChunk> chunks = List.of(chunk);
        EmbeddedCodeChunk embeddedChunk = new EmbeddedCodeChunk(chunk, new double[]{0.1, 0.2});

        // Returns the mocked list of CodeChunks when findMethodChunks is called
        when(analysisService.findMethodChunks(repositoryPath)).thenReturn(chunks);

        // Returns the mocked list of EmbeddedCodeChunks when createIndex is called
        when(embeddingIndexer.createIndex(chunks)).thenReturn(List.of(embeddedChunk));

        // First call should create the index
        List<EmbeddedCodeChunk> firstResult = semanticIndexService.getOrCreateIndex(repositoryPath);
        // Second call should reuse the existing index
        List<EmbeddedCodeChunk> secondResult = semanticIndexService.getOrCreateIndex(repositoryPath);

        // Check that the same object identity is the same for both calls
        assertSame(firstResult, secondResult);

        // Verify that the analysisService was called only once
        verify(analysisService, times(1)).findMethodChunks(repositoryPath);

        // Verify that the embeddingIndexer was called only once
        verify(embeddingIndexer, times(1)).createIndex(chunks);
    }

    @Test
    void explicitRebuildCreatesTheIndexAgain() throws IOException {
        CodeChunk chunk = createCodeChunk();
        List<CodeChunk> chunks = List.of(chunk);
        EmbeddedCodeChunk firstEmbeddedChunk = new EmbeddedCodeChunk(chunk, new double[]{0.1, 0.2});
        EmbeddedCodeChunk secondEmbeddedChunk = new EmbeddedCodeChunk(chunk, new double[]{0.3, 0.4});

        when(analysisService.findMethodChunks(repositoryPath)).thenReturn(chunks);

        // Returns different embeddings for the same chunks on subsequent calls to createIndex
        when(embeddingIndexer.createIndex(chunks)).thenReturn(List.of(firstEmbeddedChunk), List.of(secondEmbeddedChunk));

        // First call to rebuildIndex should create the index
        List<EmbeddedCodeChunk> firstResult = semanticIndexService.rebuildIndex(repositoryPath);

        // Second call to rebuildIndex should create a new index with different embeddings even when repo path has not changed
        List<EmbeddedCodeChunk> secondResult = semanticIndexService.rebuildIndex(repositoryPath);

        // Check that the first and second results use different embedded chunks (different object identity)
        assertSame(firstEmbeddedChunk, firstResult.get(0));
        assertSame(secondEmbeddedChunk, secondResult.get(0));

        // Verify that the analysisService was called twice
        verify(analysisService, times(2)).findMethodChunks(repositoryPath);

        // Verify that the embeddingIndexer was called twice
        verify(embeddingIndexer, times(2)).createIndex(chunks);
    }

    // Helper method to create a sample CodeChunk for testing
    private CodeChunk createCodeChunk() {
        return new CodeChunk(
                repositoryPath.resolve("Calculator.java"),
                "Calculator",
                "add",
                3,
                5,
                """
                public int add(int first, int second) {
                    return first + second;
                }
                """
        );
    }
}