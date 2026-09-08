package com.dando.repolens.config;

import com.dando.repolens.embedding.EmbeddingProvider;
import com.dando.repolens.embedding.OllamaEmbeddingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfiguration {

    @Bean
    public EmbeddingProvider embeddingProvider(OllamaProperties properties) {
        return new OllamaEmbeddingProvider(properties.getBaseUrl(), properties.getEmbeddingModel());
    }
}