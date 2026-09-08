package com.dando.repolens.controller;

import com.dando.repolens.config.RepositoryProperties;
import com.dando.repolens.dto.ApiErrorResponse;
import com.dando.repolens.dto.SearchResponse;
import com.dando.repolens.dto.SearchResultResponse;
import com.dando.repolens.exception.InvalidSearchRequestException;
import com.dando.repolens.exception.RepositoryNotFoundException;
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
    public SearchResponse keywordSearch(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) throws IOException {
        validateSearchRequest(query, limit);

        Path repositoryPath = repositoryProperties.resolvePath();

        if (!Files.isDirectory(repositoryPath)) {
            throw new RepositoryNotFoundException(repositoryPath);
        }

        List<SearchResult> results = searchService.keywordSearch(repositoryPath, query, limit);
        List<SearchResultResponse> responseResults = results.stream().map(result -> SearchResultResponse.from(result)).toList();

        // Format response results as a DTO
        return new SearchResponse(query, responseResults.size(), responseResults);

    }

    private void validateSearchRequest(String query, int limit) {
        if (query.isBlank()) {
            throw new InvalidSearchRequestException("Search query cannot be blank");
        }

        if (limit < 1 || limit > 20) {
            throw new InvalidSearchRequestException("Limit must be between 1 and 20");
        }
    }
}