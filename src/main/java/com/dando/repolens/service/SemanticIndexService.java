package com.dando.repolens.service;

import com.dando.repolens.embedding.CodeEmbeddingIndexer;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

// Service that manages the semantic index of code chunks for a given repository.
// It uses the RepositoryAnalysisService to find method chunks and the CodeEmbeddingIndexer to create embeddings for those chunks.
// The service caches the index for a repository and rebuilds it only when necessary.
@Service
public class SemanticIndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticIndexService.class);

    private final RepositoryAnalysisService analysisService;
    private final CodeEmbeddingIndexer embeddingIndexer;

    private Path indexedRepository;
    private List<EmbeddedCodeChunk> semanticIndex = List.of();

    public SemanticIndexService(RepositoryAnalysisService analysisService, CodeEmbeddingIndexer embeddingIndexer) {
        this.analysisService = analysisService;
        this.embeddingIndexer = embeddingIndexer;
    }

    public synchronized List<EmbeddedCodeChunk> getOrCreateIndex(Path repositoryPath) throws IOException {
        Path normalizedPath = Objects.requireNonNull(repositoryPath, "Repository path cannot be null.").toAbsolutePath().normalize();

        if (normalizedPath.equals(indexedRepository)) {
            LOGGER.info("Reusing semantic index with {} chunks", semanticIndex.size());

            return semanticIndex;
        }

        return buildIndex(normalizedPath);
    }

    // Rebuilds the semantic index for the given repository path, even if it has already been indexed
    public synchronized List<EmbeddedCodeChunk> rebuildIndex(Path repositoryPath) throws IOException {
        Path normalizedPath = Objects.requireNonNull(repositoryPath, "Repository path cannot be null.").toAbsolutePath().normalize();

        return buildIndex(normalizedPath);
    }

    private List<EmbeddedCodeChunk> buildIndex(Path repositoryPath) throws IOException {
        LOGGER.info("Building semantic index for {}", repositoryPath);

        List<CodeChunk> allChunks = analysisService.findMethodChunks(repositoryPath);

        List<EmbeddedCodeChunk> newIndex = embeddingIndexer.createIndex(allChunks);

        semanticIndex = List.copyOf(newIndex);
        indexedRepository = repositoryPath;

        LOGGER.info("Semantic index created with {} chunks", semanticIndex.size());

        return semanticIndex;
    }
}