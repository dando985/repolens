package com.dando.repolens.service;

import com.dando.repolens.embedding.CodeEmbeddingIndexer;
import com.dando.repolens.embedding.EmbeddingException;
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
    void reusesIndexForTheSameRepository() throws IOException, EmbeddingException {
        CodeChunk chunk = createCodeChunk();

        EmbeddedCodeChunk embeddedChunk = org.mockito.Mockito.mock(EmbeddedCodeChunk.class);

        List<CodeChunk> chunks = List.of(chunk);

        when(analysisService.findMethodChunks(repositoryPath)).thenReturn(chunks);

        when(embeddingIndexer.createIndex(chunks)).thenReturn(List.of(embeddedChunk));

        List<EmbeddedCodeChunk> firstResult = semanticIndexService.getOrCreateIndex(repositoryPath);

        List<EmbeddedCodeChunk> secondResult = semanticIndexService.getOrCreateIndex(repositoryPath);

        assertSame(firstResult, secondResult);

        verify(analysisService, times(1)).findMethodChunks(repositoryPath);

        verify(embeddingIndexer, times(1)).createIndex(chunks);
    }

    @Test
    void explicitRebuildCreatesTheIndexAgain() throws IOException, EmbeddingException {
        CodeChunk chunk = createCodeChunk();

        EmbeddedCodeChunk firstEmbeddedChunk = org.mockito.Mockito.mock(EmbeddedCodeChunk.class);

        EmbeddedCodeChunk secondEmbeddedChunk = org.mockito.Mockito.mock(EmbeddedCodeChunk.class);

        List<CodeChunk> chunks = List.of(chunk);

        when(analysisService.findMethodChunks(repositoryPath)).thenReturn(chunks);

        when(embeddingIndexer.createIndex(chunks)).thenReturn(List.of(firstEmbeddedChunk), List.of(secondEmbeddedChunk));

        List<EmbeddedCodeChunk> firstResult = semanticIndexService.rebuildIndex(repositoryPath);

        List<EmbeddedCodeChunk> secondResult = semanticIndexService.rebuildIndex(repositoryPath);

        assertSame(firstEmbeddedChunk, firstResult.get(0));

        assertSame(secondEmbeddedChunk, secondResult.get(0));

        verify(analysisService, times(2)).findMethodChunks(repositoryPath);

        verify(embeddingIndexer, times(2)).createIndex(chunks);
    }

    private CodeChunk createCodeChunk() {
        return new CodeChunk(repositoryPath.resolve("Calculator.java"), "Calculator", "add", 3, 5, """
                public int add(int first, int second) {
                    return first + second;
                }
                """);
    }
}