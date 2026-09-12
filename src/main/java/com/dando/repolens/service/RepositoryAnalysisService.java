package com.dando.repolens.service;

import com.dando.repolens.chunking.CodeChunker;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.JavaSourceFile;
import com.dando.repolens.scanner.RepositoryScanner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepositoryAnalysisService {

    private final RepositoryScanner repositoryScanner;
    private final CodeChunker codeChunker;

    public RepositoryAnalysisService(RepositoryScanner repositoryScanner, CodeChunker codeChunker) {
        this.repositoryScanner = repositoryScanner;
        this.codeChunker = codeChunker;
    }

    public List<CodeChunk> findMethodChunks(Path repositoryPath) throws IOException {
        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(repositoryPath);

        List<CodeChunk> allChunks = new ArrayList<>();

        for (JavaSourceFile sourceFile : sourceFiles) {
            List<CodeChunk> fileChunks = codeChunker.createChunks(sourceFile);

            allChunks.addAll(fileChunks);
        }

        return List.copyOf(allChunks);
    }
}