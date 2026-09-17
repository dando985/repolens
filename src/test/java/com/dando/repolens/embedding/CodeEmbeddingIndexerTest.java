package com.dando.repolens.embedding;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeEmbeddingIndexerTest {

    private static final double TOLERANCE = 0.000001;

    @Mock
    EmbeddingProvider embeddingProvider;

    @InjectMocks
    CodeEmbeddingIndexer embeddingIndexer;

    @Test
    void createsEmbeddingForEveryChunkAndPreservesOrder() {
        String addContent = """
                public int add(int first, int second) {
                    return first + second;
                }
                """;
        CodeChunk addChunk = createChunk("add", 3, 5, addContent);

        String subtractContent = """
                public int subtract(int first, int second) {
                    return first - second;
                }
                """;
        CodeChunk subtractChunk = createChunk("subtract", 7, 9, subtractContent);

        double[] addEmbedding = {0.1, 0.2, 0.3};
        double[] subtractEmbedding = {0.4, 0.5, 0.6};

        String addEmbeddingText = createExpectedEmbeddingText("add", addContent);
        String subtractEmbeddingText = createExpectedEmbeddingText("subtract", subtractContent);

        when(embeddingProvider.createEmbedding(addEmbeddingText)).thenReturn(addEmbedding);
        when(embeddingProvider.createEmbedding(subtractEmbeddingText)).thenReturn(subtractEmbedding);

        List<EmbeddedCodeChunk> index = embeddingIndexer.createIndex(List.of(addChunk, subtractChunk));

        // Check that only 2 embedded code chunks are returned
        assertEquals(2, index.size());

        // Check that expected and returned code chunks are the same along with their embedding vectors
        assertSame(addChunk, index.get(0).getChunk());
        assertArrayEquals(addEmbedding, index.get(0).getEmbedding(), TOLERANCE);

        assertSame(subtractChunk, index.get(1).getChunk());
        assertArrayEquals(subtractEmbedding, index.get(1).getEmbedding(), TOLERANCE);

        verify(embeddingProvider).createEmbedding(addEmbeddingText);
        verify(embeddingProvider).createEmbedding(subtractEmbeddingText);
    }

    @Test
    void emptyChunkListCreatesEmptyIndex() {
        List<EmbeddedCodeChunk> index = embeddingIndexer.createIndex(List.of());

        assertTrue(index.isEmpty());

        verifyNoInteractions(embeddingProvider);
    }

    // Helper to create a sample code chunk
    private CodeChunk createChunk(String methodName, int startLine, int endLine, String content) {
        return new CodeChunk(
                Path.of("src", "Calculator.java"),
                "Calculator",
                methodName,
                startLine,
                endLine,
                content
        );
    }

    // Helper to create a correctly formatted text representation of a code chunk and its metadata to be used for embedding generation
    private String createExpectedEmbeddingText(String methodName, String content) {
        String lineSeparator = System.lineSeparator();

        return "File: Calculator.java" + lineSeparator
                + "Class: Calculator" + lineSeparator
                + "Method: " + methodName + lineSeparator
                + "Code:" + lineSeparator
                + content;
    }
}