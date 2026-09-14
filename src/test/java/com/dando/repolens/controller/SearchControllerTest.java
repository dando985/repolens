package com.dando.repolens.controller;

import com.dando.repolens.config.RepositoryProperties;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.SearchResult;
import com.dando.repolens.service.RepositorySearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RepositorySearchService searchService;

    @MockitoBean
    RepositoryProperties repositoryProperties;

    @TempDir
    Path temporaryRepository;

    @BeforeEach
    void setUp() {
        when(repositoryProperties.resolvePath()).thenReturn(temporaryRepository);
    }

    @Test
    void keywordSearchReturnsMappedResults() throws Exception {
        SearchResult searchResult = createSearchResult();

        when(searchService.keywordSearch(temporaryRepository, "add", 5)).thenReturn(List.of(searchResult));

        // Perform a GET request to the /api/search/keyword endpoint with sample SearchResult data
        mockMvc.perform(get("/api/search/keyword")
                .param("query", "add")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("add"))
                .andExpect(jsonPath("$.resultCount").value(1))
                .andExpect(jsonPath("$.results[0].score").value(4.0))
                .andExpect(jsonPath("$.results[0].file").value("Calculator.java"))
                .andExpect(jsonPath("$.results[0].className").value("Calculator"))
                .andExpect(jsonPath("$.results[0].methodName").value("add"))
                .andExpect(jsonPath("$.results[0].startLine").value(3))
                .andExpect(jsonPath("$.results[0].endLine").value(5));

        // Check that the searchService was called with the correct parameters
        verify(searchService).keywordSearch(temporaryRepository, "add", 5);
    }

    @Test
    void semanticSearchReturnsMappedResults() throws Exception {
        SearchResult searchResult = createSearchResult();

        when(searchService.semanticSearch(temporaryRepository, "combine two numbers", 3)).thenReturn(List.of(searchResult));

        // Perform a GET request to the /api/search/semantic endpoint with sample SearchResult data
        mockMvc.perform(get("/api/search/semantic").param("query", "combine two numbers").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("combine two numbers"))
                .andExpect(jsonPath("$.resultCount").value(1))
                .andExpect(jsonPath("$.results[0].methodName").value("add"));

        // Check that the searchService was called with the correct parameters
        verify(searchService).semanticSearch(temporaryRepository, "combine two numbers", 3);
    }

    @Test
    void blankQueryReturnsBadRequest() throws Exception {
        // Perform a GET request to the /api/search/keyword endpoint with a blank query
        mockMvc.perform(get("/api/search/keyword").param("query", " ").param("limit", "5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid search request"))
                .andExpect(jsonPath("$.details").value("Search query cannot be blank"));
    }

    @Test
    void invalidLimitReturnsBadRequest() throws Exception {
        // Perform a GET request to the /api/search/keyword endpoint with an invalid limit
        mockMvc.perform(get("/api/search/keyword").param("query", "add").param("limit", "21"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid search request"))
                .andExpect(jsonPath("$.details").value("Limit must be between 1 and 20"));
    }

    @Test
    void missingRepositoryReturnsNotFound() throws Exception {
        Path missingRepository = temporaryRepository.resolve("does-not-exist");

        when(repositoryProperties.resolvePath()).thenReturn(missingRepository);

        // Perform a GET request to the /api/search/keyword endpoint when the repository directory is missing
        mockMvc.perform(get("/api/search/keyword").param("query", "add"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Repository directory was not found"));
    }

    // Helper method to create a sample SearchResult for testing
    private SearchResult createSearchResult() {
        CodeChunk chunk = new CodeChunk(Path.of("Calculator.java"),
                "Calculator",
                "add",
                3,
                5,
                """
                public int add(int first, int second) {
                    return first + second;
                }
                """);

        return new SearchResult(chunk, 4.0);
    }
}