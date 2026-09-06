package com.dando.repolens.dto;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.SearchResult;

public record SearchResultResponse(
        double score,
        String file,
        String className,
        String methodName,
        int startLine,
        int endLine,
        String content
) {

    public static SearchResultResponse from(SearchResult result) {
        CodeChunk chunk = result.getChunk();

        return new SearchResultResponse(
                result.getScore(),
                chunk.getFilePath().getFileName().toString(),
                chunk.getClassName(),
                chunk.getMethodName(),
                chunk.getStartLine(),
                chunk.getEndLine(),
                chunk.getContent()
        );
    }
}