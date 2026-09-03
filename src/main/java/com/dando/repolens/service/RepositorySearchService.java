package com.dando.repolens.service;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.retrieval.KeywordCodeRetriever;
import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class RepositorySearchService {

    private final RepositoryAnalysisService analysisService;

    public RepositorySearchService(RepositoryAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    public List<SearchResult> keywordSearch(Path repositoryPath, String query, int limit) throws IOException {
        List<CodeChunk> chunks = analysisService.findMethodChunks(repositoryPath);

        // Create a KeywordCodeRetriever with the list of code chunks
        KeywordCodeRetriever retriever = new KeywordCodeRetriever(chunks);

        // Create a SearchQuery object with the query and limit
        SearchQuery searchQuery = new SearchQuery(query, limit);

        // Perform keyword search matching with the query and return the results
        return retriever.search(searchQuery);
    }
}