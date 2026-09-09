package com.dando.repolens.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "repolens.repository")
public class RepositoryProperties {

    // Define path property (defined in application.properties as repolens.repository.path)
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // Resolve the path to an absolute and normalized Path object
    public Path resolvePath() {
        // Check if usable path is set
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("Missing required property: repolens.repository.path");
        }

        return Path.of(path).toAbsolutePath().normalize();
    }
}