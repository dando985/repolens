package com.dando.repolens.exception;

import java.nio.file.Path;

public class RepositoryNotFoundException extends RuntimeException {

    public RepositoryNotFoundException(Path path) {
        super("Repository directory was not found: " + path);
    }
}