package com.dando.repolens.controller;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.service.RepositorySearchService;
import com.dando.repolens.model.SearchResult;
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
import java.util.Map;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final RepositorySearchService searchService;

    public SearchController(RepositorySearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/keyword")
    public ResponseEntity<Map<String, Object>> keywordSearch(@RequestParam String query, @RequestParam(defaultValue = "5") int limit) {
        if (query.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Search query cannot be blank"));
        }

        // Limit the number of results to between 1 and 20
        if (limit < 1 || limit > 20) {
            return ResponseEntity.badRequest().body(Map.of("error", "Limit must be between 1 and 20"));
        }

        Path repositoryPath = getRepositoryPath();

        if (!Files.isDirectory(repositoryPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Repository directory was not found", "path", repositoryPath.toString()));
        }

        try {
            List<SearchResult> results = searchService.keywordSearch(repositoryPath, query, limit);

            List<Map<String, Object>> responseResults = results.stream().map(this::createResultResponse).toList();

            return ResponseEntity.ok(Map.of(
                    "query", query,
                    "resultCount", responseResults.size(),
                    "results", responseResults));
        } catch (IOException exception) {
            String details = exception.getMessage() == null ? "No additional details" : exception.getMessage();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unable to search repository", "details", details));
        }
    }

    private Map<String, Object> createResultResponse(SearchResult result) {
        CodeChunk chunk = result.getChunk();

        return Map.of(
                "score", result.getScore(),
                "file", chunk.getFilePath().getFileName().toString(),
                "className", chunk.getClassName(),
                "methodName", chunk.getMethodName(),
                "startLine", chunk.getStartLine(),
                "endLine", chunk.getEndLine(),
                "content", chunk.getContent());
    }

    private Path getRepositoryPath() {
        return Path.of("sample-project").toAbsolutePath().normalize();
    }
}