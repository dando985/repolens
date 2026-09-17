package com.dando.repolens.service;

import com.dando.repolens.embedding.EmbeddingProvider;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import com.dando.repolens.model.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositorySearchServiceTest {

    private static final double TOLERANCE = 0.000001;

    @Mock
    RepositoryAnalysisService analysisService;

    @Mock
    SemanticIndexService semanticIndexService;

    @Mock
    EmbeddingProvider embeddingProvider;

    @InjectMocks
    RepositorySearchService searchService;

    @TempDir
    Path repositoryPath;

    @Test
    void keywordSearchUsesAnalyzedMethodChunks() throws IOException {
        CodeChunk weakMatch = createChunk(
                "NotificationService",
                "sendEmail",
                """
                public void sendEmail(User user) {
                    emailClient.send(user.getEmail());
                }
                """
        );

        CodeChunk strongMatch = createChunk(
                "UserService",
                "findUser",
                """
                public User findUser(String id) {
                    return users.get(id);
                }
                """
        );

        when(analysisService.findMethodChunks(repositoryPath)).thenReturn(List.of(weakMatch, strongMatch));

        List<SearchResult> results = searchService.keywordSearch(repositoryPath, "user", 2);

        assertEquals(2, results.size());

        assertSame(strongMatch, results.get(0).getChunk());
        assertEquals(6.0, results.get(0).getScore(), TOLERANCE);

        assertSame(weakMatch, results.get(1).getChunk());
        assertEquals(1.0, results.get(1).getScore(), TOLERANCE);

        verify(analysisService).findMethodChunks(repositoryPath);

        verifyNoInteractions(semanticIndexService, embeddingProvider);
    }

    @Test
    void semanticSearchUsesSemanticIndexAndEmbeddingProvider() throws IOException {
        CodeChunk unrelatedChunk = createChunk("OrderService", "calculateTotal", "double calculateTotal() {}");
        EmbeddedCodeChunk unrelatedEmbeddedChunk = new EmbeddedCodeChunk(unrelatedChunk, new double[]{0.0, 1.0});

        CodeChunk exactMatchChunk = createChunk("UserService", "findUser", "User findUser() {}");
        EmbeddedCodeChunk exactEmbeddedChunk = new EmbeddedCodeChunk(exactMatchChunk, new double[]{1.0, 0.0});

        when(semanticIndexService.getOrCreateIndex(repositoryPath)).thenReturn(List.of(unrelatedEmbeddedChunk, exactEmbeddedChunk));
        when(embeddingProvider.createEmbedding("find a user")).thenReturn(new double[]{1.0, 0.0});

        List<SearchResult> results = searchService.semanticSearch(repositoryPath, "find a user", 1);

        assertEquals(1, results.size());

        assertSame(exactMatchChunk, results.get(0).getChunk());
        assertEquals(1.0, results.get(0).getScore(), TOLERANCE);

        verify(semanticIndexService).getOrCreateIndex(repositoryPath);
        verify(embeddingProvider).createEmbedding("find a user");
        verifyNoInteractions(analysisService);
    }

    @Test
    void invalidKeywordQueryIsRejectedBeforeAnalysis() {
        assertThrows(IllegalArgumentException.class, () -> searchService.keywordSearch(repositoryPath, " ", 5));

        verifyNoInteractions(analysisService, semanticIndexService, embeddingProvider);
    }

    @Test
    void invalidSemanticLimitIsRejectedBeforeIndexing() {
        assertThrows(IllegalArgumentException.class, () -> searchService.semanticSearch(repositoryPath, "find a user", 0));

        verifyNoInteractions(analysisService, semanticIndexService, embeddingProvider);
    }

    // Helper function to create sample code chunk
    private CodeChunk createChunk(String className, String methodName, String content) {
        return new CodeChunk(
                repositoryPath.resolve(className + ".java"),
                className,
                methodName,
                1,
                3,
                content
        );
    }
}