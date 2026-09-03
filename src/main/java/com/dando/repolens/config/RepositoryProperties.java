package com.dando.repolens.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "repolens.repository")
public class RepositoryProperties {

    // Define path property
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path resolvePath() {
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Missing required property: repolens.repository.path");
        }

        return Path.of(path).toAbsolutePath().normalize();
    }
}