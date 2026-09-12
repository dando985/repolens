package com.dando.repolens.service;

import com.dando.repolens.embedding.EmbeddingProvider;
import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.EmbeddedCodeChunk;
import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;
import com.dando.repolens.retrieval.CodeRetriever;
import com.dando.repolens.retrieval.KeywordCodeRetriever;
import com.dando.repolens.retrieval.SemanticCodeRetriever;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class RepositorySearchService {

    private final RepositoryAnalysisService analysisService;
    private final SemanticIndexService semanticIndexService;
    private final EmbeddingProvider embeddingProvider;

    public RepositorySearchService(RepositoryAnalysisService analysisService, SemanticIndexService semanticIndexService, EmbeddingProvider embeddingProvider) {
        this.analysisService = analysisService;
        this.semanticIndexService = semanticIndexService;
        this.embeddingProvider = embeddingProvider;
    }

    public List<SearchResult> keywordSearch(Path repositoryPath, String query, int limit) throws IOException {
        // Create a SearchQuery object with the query and limit
        SearchQuery searchQuery = new SearchQuery(query, limit);

        // Retrieve all code chunks from the repository
        List<CodeChunk> chunks = analysisService.findMethodChunks(repositoryPath);

        // Initialize the retriever with the list of code chunks
        KeywordCodeRetriever retriever = new KeywordCodeRetriever(chunks);

        // Perform keyword search matching with the query and return the results
        return retriever.search(searchQuery);
    }

    public List<SearchResult> semanticSearch(Path repositoryPath, String query, int limit) throws IOException {
        // Create a SearchQuery object with the query and limit
        SearchQuery searchQuery = new SearchQuery(query, limit);

        // Retrieve or create the semantic index for the repository
        List<EmbeddedCodeChunk> semanticIndex = semanticIndexService.getOrCreateIndex(repositoryPath);

        // Initialize the retriever with the embedding provider and the semantic index
        SemanticCodeRetriever retriever = new SemanticCodeRetriever(embeddingProvider, semanticIndex);

        // Perform semantic search matching with the query and return the results
        return retriever.search(searchQuery);
    }

}