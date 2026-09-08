package com.dando.repolens.service;

import com.dando.repolens.embedding.CodeEmbeddingIndexer;
import com.dando.repolens.embedding.EmbeddingException;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class SemanticIndexService {

    private static final Logger logger = LoggerFactory.getLogger(SemanticIndexService.class);

    private final RepositoryAnalysisService analysisService;
    private final CodeEmbeddingIndexer embeddingIndexer;

    private boolean initialized;
    private Path indexedRepository;
    private List<EmbeddedCodeChunk> semanticIndex = List.of();

    public SemanticIndexService(RepositoryAnalysisService analysisService, CodeEmbeddingIndexer embeddingIndexer) {
        this.analysisService = analysisService;
        this.embeddingIndexer = embeddingIndexer;
    }

    public synchronized List<EmbeddedCodeChunk> getOrCreateIndex(Path repositoryPath) throws IOException, EmbeddingException {
        Path normalizedPath = repositoryPath.toAbsolutePath().normalize();

        boolean correctRepository = normalizedPath.equals(indexedRepository);

        if (initialized && correctRepository) {
            logger.info("Reusing semantic index with {} chunks", semanticIndex.size());

            return semanticIndex;
        }

        return buildIndex(normalizedPath);
    }

    public synchronized List<EmbeddedCodeChunk> rebuildIndex(Path repositoryPath) throws IOException, EmbeddingException {
        Path normalizedPath = repositoryPath.toAbsolutePath().normalize();

        return buildIndex(normalizedPath);
    }

    private List<EmbeddedCodeChunk> buildIndex(Path repositoryPath) throws IOException, EmbeddingException {
        logger.info("Building semantic index for {}", repositoryPath);

        List<CodeChunk> chunks = analysisService.findMethodChunks(repositoryPath);

        List<EmbeddedCodeChunk> newIndex = embeddingIndexer.createIndex(chunks);

        semanticIndex = List.copyOf(newIndex);
        indexedRepository = repositoryPath;
        initialized = true;

        logger.info("Semantic index created with {} chunks", semanticIndex.size());

        return semanticIndex;
    }
}