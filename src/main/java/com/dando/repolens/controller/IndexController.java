package com.dando.repolens.controller;

import com.dando.repolens.config.RepositoryProperties;
import com.dando.repolens.dto.SemanticIndexResponse;
import com.dando.repolens.embedding.EmbeddingException;
import com.dando.repolens.embedding.EmbeddingProvider;
import com.dando.repolens.exception.RepositoryNotFoundException;
import com.dando.repolens.model.EmbeddedCodeChunk;
import com.dando.repolens.service.SemanticIndexService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/index")
public class IndexController {

    private final SemanticIndexService semanticIndexService;
    private final RepositoryProperties repositoryProperties;
    private final EmbeddingProvider embeddingProvider;

    public IndexController(SemanticIndexService semanticIndexService, RepositoryProperties repositoryProperties, EmbeddingProvider embeddingProvider) {
        this.semanticIndexService = semanticIndexService;
        this.repositoryProperties = repositoryProperties;
        this.embeddingProvider = embeddingProvider;
    }

    @PostMapping("/semantic/rebuild")
    public SemanticIndexResponse rebuildSemanticIndex() throws IOException, EmbeddingException {
        Path repositoryPath = repositoryProperties.resolvePath();

        if (!Files.isDirectory(repositoryPath)) {
            throw new RepositoryNotFoundException(repositoryPath);
        }

        List<EmbeddedCodeChunk> index = semanticIndexService.rebuildIndex(repositoryPath);

        return new SemanticIndexResponse("rebuilt", repositoryPath.toString(), embeddingProvider.getModelName(), index.size());
    }
}