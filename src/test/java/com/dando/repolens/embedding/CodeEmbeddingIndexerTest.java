package com.dando.repolens.embedding;

import com.dando.repolens.exception.EmbeddingException;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
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

        String subtractContent = """
                public int subtract(int first, int second) {
                    return first - second;
                }
                """;

        CodeChunk addChunk = createChunk("add", 3, 5, addContent);

        CodeChunk subtractChunk = createChunk("subtract", 7, 9, subtractContent);

        double[] addEmbedding = {0.1, 0.2, 0.3};
        double[] subtractEmbedding = {0.4, 0.5, 0.6};

        String addEmbeddingText = createExpectedEmbeddingText("add", addContent);

        String subtractEmbeddingText = createExpectedEmbeddingText("subtract", subtractContent);

        when(embeddingProvider.createEmbedding(anyString())).thenReturn(addEmbedding, subtractEmbedding);

        List<EmbeddedCodeChunk> index = embeddingIndexer.createIndex(List.of(addChunk, subtractChunk));

        assertEquals(2, index.size());

        assertSame(addChunk, index.get(0).getChunk());
        assertArrayEquals(addEmbedding, index.get(0).getEmbedding(), TOLERANCE);

        assertSame(subtractChunk, index.get(1).getChunk());
        assertArrayEquals(subtractEmbedding, index.get(1).getEmbedding(), TOLERANCE);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);

        verify(embeddingProvider, times(2)).createEmbedding(textCaptor.capture());

        List<String> actualEmbeddingTexts = textCaptor.getAllValues();

        assertEquals(normalizeLineEndings(addEmbeddingText), normalizeLineEndings(actualEmbeddingTexts.get(0)));

        assertEquals(normalizeLineEndings(subtractEmbeddingText), normalizeLineEndings(actualEmbeddingTexts.get(1)));
    }

    @Test
    void emptyChunkListCreatesEmptyIndex() {
        List<EmbeddedCodeChunk> index = embeddingIndexer.createIndex(List.of());

        assertTrue(index.isEmpty());

        verifyNoInteractions(embeddingProvider);
    }

    @Test
    void propagatesEmbeddingProviderFailure() {
        CodeChunk chunk = createChunk(
                "add",
                3,
                5,
                """
                public int add(int first, int second) {
                    return first + second;
                }
                """
        );

        EmbeddingException expectedException = new EmbeddingException("Unable to create embedding");

        when(embeddingProvider.createEmbedding(anyString())).thenThrow(expectedException);

        EmbeddingException actualException = assertThrows(EmbeddingException.class, () -> embeddingIndexer.createIndex(List.of(chunk)));

        assertSame(expectedException, actualException);
    }

    // Helper function to create sample code chunk
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

    // Helper function to create expected text representation of a code chunk and its metadata
    private String createExpectedEmbeddingText(String methodName, String content) {
        String lineSeparator = System.lineSeparator();

        return "File: Calculator.java" + lineSeparator
                + "Class: Calculator" + lineSeparator
                + "Method: " + methodName + lineSeparator
                + "Code:" + lineSeparator
                + content;
    }

    // Helper function to get rid of invisible line endings
    private String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace('\r', '\n');
    }
}