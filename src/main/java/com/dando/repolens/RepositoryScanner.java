package com.dando.repolens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Utility class that scans a repository path and creates a list of Java source files
public class RepositoryScanner {

    public List<JavaSourceFile> scan(Path repositoryPath) throws IOException {

        List<JavaSourceFile> sourceFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(repositoryPath)) {
            List<Path> javaFilePaths = paths
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            for (Path javaFilePath : javaFilePaths) {
                String content = Files.readString(javaFilePath);
                JavaSourceFile sourceFile = new JavaSourceFile(javaFilePath, content);
                sourceFiles.add(sourceFile);
            }
        }
        return sourceFiles;
    }
}