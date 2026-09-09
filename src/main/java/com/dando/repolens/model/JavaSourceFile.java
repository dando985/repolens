package com.dando.repolens.model;

import java.nio.file.Path;

// Maps a Java source file to its path and source code.
public class JavaSourceFile {

    private final Path path;
    private final String content;

    public JavaSourceFile(Path path, String content) {
        this.path = path;
        this.content = content;
    }

    public Path getPath() {
        return path;
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    public String getContent() {
        return content;
    }
}