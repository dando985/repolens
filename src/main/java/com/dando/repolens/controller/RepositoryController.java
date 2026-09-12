package com.dando.repolens.controller;

import com.dando.repolens.config.RepositoryProperties;
import com.dando.repolens.exception.RepositoryNotFoundException;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import com.dando.repolens.scanner.RepositoryScanner;
import com.dando.repolens.service.RepositoryAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

// REST controller that provides endpoints to scan a Java repository for source files and analyze methods within those files.
@RestController
@RequestMapping("/api/repository")
public class RepositoryController {

    // Inject RepositoryScanner implementation to scan the repository for Java source files
    private final RepositoryScanner repositoryScanner;

    // Use RepositoryAnalysisService to analyze the repository and find method chunks
    private final RepositoryAnalysisService analysisService;

    // Defines configured repository path from application.properties file
    private final RepositoryProperties repositoryProperties;

    public RepositoryController(RepositoryScanner repositoryScanner, RepositoryAnalysisService analysisService, RepositoryProperties repositoryProperties) {
        this.repositoryScanner = repositoryScanner;
        this.analysisService = analysisService;
        this.repositoryProperties = repositoryProperties;
    }

    @GetMapping("/scan")
    public Map<String, Object> scanRepository() throws IOException {
        // Verify that the repository path is valid and exists
        Path repositoryPath = requireRepositoryPath();

        // Scan the repository for Java source files using RepositoryScanner service
        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(repositoryPath);

        // Create a list of relative file names for the HTTP response
        List<String> fileNames = sourceFiles.stream().map(sourceFile -> repositoryPath.relativize(sourceFile.getPath()).toString()).toList();

        // HTTP response
        return Map.of(
                "repository", repositoryPath.toString(),
                "javaFileCount", sourceFiles.size(),
                "files", fileNames);
    }

    @GetMapping("/methods")
    public Map<String, Object> listMethods() throws IOException {
        // Verify that the repository path is valid and exists
        Path repositoryPath = requireRepositoryPath();

        // Use RepositoryAnalysisService to scan and chunk methods in the repository
        List<CodeChunk> chunks = analysisService.findMethodChunks(repositoryPath);

        // Create a list of method names for the HTTP response
        List<String> methods = chunks.stream().map(chunk -> chunk.getClassName() + "." + chunk.getMethodName()).toList();

        // HTTP response
        return Map.of(
                "repository", repositoryPath.toString(),
                "methodChunkCount", chunks.size(),
                "methods", methods);
    }

    private Path requireRepositoryPath() {
        Path repositoryPath = repositoryProperties.resolvePath();

        if (!Files.isDirectory(repositoryPath)) {
            throw new RepositoryNotFoundException(repositoryPath);
        }

        return repositoryPath;
    }
}