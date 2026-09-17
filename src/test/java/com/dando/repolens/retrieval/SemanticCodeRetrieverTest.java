package com.dando.repolens.retrieval;

import com.dando.repolens.embedding.EmbeddingProvider;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SemanticCodeRetrieverTest {

    private static final double TOLERANCE = 0.000001;

    @Mock
    EmbeddingProvider embeddingProvider;

    @Test
    void ranksChunksByDescendingSimilarity() {
        // Initialize sample semantic index with embedded code chunks
        List<EmbeddedCodeChunk> semanticIndex = createSemanticIndex();

        SemanticCodeRetriever retriever = new SemanticCodeRetriever(embeddingProvider, semanticIndex);

        when(embeddingProvider.createEmbedding("find a user")).thenReturn(new double[]{1.0, 0.0});

        // Set query with high max search results limit
        SearchQuery query = new SearchQuery("find a user", 10);

        List<SearchResult> results = retriever.search(query);

        // Check that only 3 results (exactMatch, partialMatch, unrelated) are returned
        assertEquals(3, results.size());

        // First top match
        assertEquals("exactMatch", results.get(0).getChunk().getMethodName());
        assertEquals(1.0, results.get(0).getScore(), TOLERANCE);

        // Second top match
        assertEquals("partialMatch", results.get(1).getChunk().getMethodName());
        assertEquals(1.0 / Math.sqrt(2.0), results.get(1).getScore(), TOLERANCE);

        // Third top match
        assertEquals("unrelated", results.get(2).getChunk().getMethodName());
        assertEquals(0.0, results.get(2).getScore(), TOLERANCE);

        // Check that the embedding provider was called with the correct query text
        verify(embeddingProvider).createEmbedding("find a user");
    }

    @Test
    void respectsRequestedResultLimit() {
        // Initialize sample semantic index with embedded code chunks
        List<EmbeddedCodeChunk> semanticIndex = createSemanticIndex();

        SemanticCodeRetriever retriever = new SemanticCodeRetriever(embeddingProvider, semanticIndex);

        when(embeddingProvider.createEmbedding("find a user")).thenReturn(new double[]{1.0, 0.0});

        // Set query with limited max search results to 2
        SearchQuery query = new SearchQuery("find a user", 2);

        List<SearchResult> results = retriever.search(query);

        // Check that only top 2 results are returned
        assertEquals(2, results.size());

        assertEquals("exactMatch", results.get(0).getChunk().getMethodName());

        assertEquals("partialMatch", results.get(1).getChunk().getMethodName());
    }

    @Test
    void emptyIndexReturnsEmptyResultsWithoutCreatingEmbedding() {
        SemanticCodeRetriever retriever = new SemanticCodeRetriever(embeddingProvider, List.of());

        SearchQuery query = new SearchQuery("find a user", 5);

        List<SearchResult> results = retriever.search(query);

        // Check that results are empty
        assertTrue(results.isEmpty());

        // Check that embedding provider was not called because embedded chunks was empty
        verifyNoInteractions(embeddingProvider);
    }

    // Helper method to create sample semantic index with embedded code chunks for testing
    private List<EmbeddedCodeChunk> createSemanticIndex() {
        return List.of(
                createEmbeddedChunk("unrelated", new double[]{0.0, 1.0}),
                createEmbeddedChunk("exactMatch", new double[]{1.0, 0.0}),
                createEmbeddedChunk("partialMatch", new double[]{1.0, 1.0})
        );
    }

    // Helper method to create an EmbeddedCodeChunk with a given method name and embedding vector
    private EmbeddedCodeChunk createEmbeddedChunk(String methodName, double[] embedding) {
        CodeChunk chunk = new CodeChunk(
                Path.of("Example.java"),
                "Example",
                methodName,
                1,
                3,
                "void " + methodName + "() {}"
        );

        return new EmbeddedCodeChunk(chunk, embedding);
    }
}