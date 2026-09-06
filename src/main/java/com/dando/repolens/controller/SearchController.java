package com.dando.repolens.controller;

import com.dando.repolens.config.RepositoryProperties;
import com.dando.repolens.dto.ApiErrorResponse;
import com.dando.repolens.dto.SearchResponse;
import com.dando.repolens.dto.SearchResultResponse;
import com.dando.repolens.model.SearchResult;
import com.dando.repolens.service.RepositorySearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final RepositorySearchService searchService;
    private final RepositoryProperties repositoryProperties;

    public SearchController(RepositorySearchService searchService, RepositoryProperties repositoryProperties) {
        this.searchService = searchService;
        this.repositoryProperties = repositoryProperties;
    }

    @GetMapping("/keyword")
    public ResponseEntity<?> keywordSearch(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
        if (query.isBlank()) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("Search query cannot be blank"));
        }

        // Limit the number of results to between 1 and 20
        if (limit < 1 || limit > 20) {
            return ResponseEntity.badRequest().body(new ApiErrorResponse("Limit must be between 1 and 20"));
        }

        Path repositoryPath = getRepositoryPath();

        if (!Files.isDirectory(repositoryPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("Repository directory was not found", repositoryPath.toString()));
        }

        try {
            List<SearchResult> results = searchService.keywordSearch(repositoryPath, query, limit);
            List<SearchResultResponse> responseResults = results.stream().map(result -> SearchResultResponse.from(result)).toList();

            SearchResponse response = new SearchResponse(query, responseResults.size(), responseResults);

            return ResponseEntity.ok(response);
        } catch (IOException exception) {
            String details = exception.getMessage() == null ? "No additional details" : exception.getMessage();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiErrorResponse("Unable to search repository", details));
        }
    }

    private Path getRepositoryPath() {
        return repositoryProperties.resolvePath();
    }
}