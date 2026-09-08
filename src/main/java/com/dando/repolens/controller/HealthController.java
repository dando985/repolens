package com.dando.repolens.controller;

import com.dando.repolens.embedding.EmbeddingException;
import com.dando.repolens.embedding.EmbeddingProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final EmbeddingProvider embeddingProvider;

    public HealthController(EmbeddingProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "RepoLens");
    }

    @GetMapping("/health/ollama")
    public Map<String, Object> ollamaHealth() throws EmbeddingException {
        double[] embedding = embeddingProvider.createEmbedding("RepoLens health check");

        return Map.of("status", "UP", "model", embeddingProvider.getModelName(), "dimensions", embedding.length);
    }
}