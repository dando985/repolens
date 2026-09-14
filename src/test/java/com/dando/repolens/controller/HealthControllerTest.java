package com.dando.repolens.controller;

import com.dando.repolens.exception.EmbeddingException;
import com.dando.repolens.embedding.EmbeddingProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EmbeddingProvider embeddingProvider;

    @Test
    void healthEndpointReturnsApplicationStatus() throws Exception {

        // Check health endpoint response body
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("RepoLens"));

        // Ensure that the embedding provider was not called during this test
        verifyNoInteractions(embeddingProvider);
    }

    @Test
    void ollamaHealthEndpointReturnsModelInformation() throws Exception {
        // Mock the embedding provider to return a dummy embedding and model name
        when(embeddingProvider.createEmbedding(anyString())).thenReturn(new double[768]);
        when(embeddingProvider.getModelName()).thenReturn("embeddinggemma");

        // Check ollama health endpoint response body
        mockMvc.perform(get("/api/health/ollama"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.model").value("embeddinggemma"))
                .andExpect(jsonPath("$.dimensions").value(768));
    }

    @Test
    void ollamaFailureReturnsBadGateway() throws Exception {
        // Mock the embedding provider to throw an EmbeddingException when called
        when(embeddingProvider.createEmbedding(anyString()))
                .thenThrow(
                        new EmbeddingException(
                                "Unable to connect to Ollama"
                        )
                );

        // Check that the ollama health endpoint returns a 502 Bad Gateway status and the expected error message
        mockMvc.perform(get("/api/health/ollama"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Embedding provider is unavailable"))
                .andExpect(jsonPath("$.details").value("Unable to connect to Ollama"));
    }
}