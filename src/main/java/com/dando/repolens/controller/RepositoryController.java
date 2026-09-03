package com.dando.repolens.controller;

import com.dando.repolens.JavaSourceFile;
import com.dando.repolens.RepositoryScanner;
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

    public RepositoryController(RepositoryScanner repositoryScanner) {
        this.repositoryScanner = repositoryScanner;
    }

    @GetMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanRepository() {
        // Determine repository path
        Path repositoryPath = Path.of("sample-project").toAbsolutePath().normalize();

        if (!Files.isDirectory(repositoryPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Repository directory was not found", "path", repositoryPath.toString()));
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
}