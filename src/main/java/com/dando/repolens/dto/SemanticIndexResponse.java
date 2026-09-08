package com.dando.repolens.dto;

public record SemanticIndexResponse(String status, String repository, String model, int chunkCount) {
}