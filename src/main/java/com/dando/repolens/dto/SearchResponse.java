package com.dando.repolens.dto;

import java.util.List;

public record SearchResponse(String query, int resultCount, List<SearchResultResponse> results) {
}