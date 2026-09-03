package com.dando.repolens.controller;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import com.dando.repolens.service.RepositoryAnalysisService;
import com.dando.repolens.scanner.RepositoryScanner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repository")
public class RepositoryController {

    private final RepositoryScanner repositoryScanner;
    private final RepositoryAnalysisService analysisService;

    public RepositoryController(RepositoryScanner repositoryScanner, RepositoryAnalysisService analysisService) {
        this.repositoryScanner = repositoryScanner;
        this.analysisService = analysisService;
    }

    @GetMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanRepository() {
        // Determine repository path
        Path repositoryPath = getRepositoryPath();

        if (!Files.isDirectory(repositoryPath)) {
            return repositoryNotFound(repositoryPath);
        }

        try {
            // Scan the repository for Java source files using RepositoryScanner service
            List<JavaSourceFile> sourceFiles = repositoryScanner.scan(repositoryPath);

            // Create a list of relative file names for the HTTP response
            List<String> fileNames = sourceFiles.stream().map(sourceFile -> repositoryPath.relativize(sourceFile.getPath()).toString()).toList();

            // HTTP response
            return ResponseEntity.ok(Map.of(
                    "repository", repositoryPath.toString(),
                    "javaFileCount", sourceFiles.size(),
                    "files", fileNames));

        } catch (IOException exception) {
            String details = exception.getMessage() == null ? "No additional details" : exception.getMessage();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unable to scan repository", "details", details));
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> findMethods() {
        // Determine repository path
        Path repositoryPath = getRepositoryPath();

        if (!Files.isDirectory(repositoryPath)) {
            return repositoryNotFound(repositoryPath);
        }

        try {
            // Use RepositoryAnalysisService to scan and chunk methods in the repository
            List<CodeChunk> chunks = analysisService.findMethodChunks(repositoryPath);

            // Create a list of method names for the HTTP response
            List<String> methods = chunks.stream().map(chunk -> chunk.getClassName() + "." + chunk.getMethodName()).toList();

            // HTTP response
            return ResponseEntity.ok(Map.of(
                    "repository", repositoryPath.toString(),
                    "methodChunkCount", chunks.size(),
                    "methods", methods));
        } catch (IOException exception) {
            return processingError(exception);
        }
    }

    private Path getRepositoryPath() {
        return Path.of("sample-project").toAbsolutePath().normalize();
    }

    private ResponseEntity<Map<String, Object>> repositoryNotFound(Path repositoryPath) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Repository directory was not found", "path", repositoryPath.toString()));
    }

    private ResponseEntity<Map<String, Object>> processingError(IOException exception) {
        String details = exception.getMessage() == null ? "No additional details" : exception.getMessage();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unable to process repository", "details", details));
    }
}