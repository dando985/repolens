package com.dando.repolens.model;

import java.nio.file.Path;

public class CodeChunk {

    private final Path filePath;
    private final String className;
    private final String methodName;
    private final int startLine;
    private final int endLine;
    private final String content;

    public CodeChunk(Path filePath, String className, String methodName, int startLine, int endLine, String content) {
        this.filePath = filePath;
        this.className = className;
        this.methodName = methodName;
        this.startLine = startLine;
        this.endLine = endLine;
        this.content = content;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getContent() {
        return content;
    }
}