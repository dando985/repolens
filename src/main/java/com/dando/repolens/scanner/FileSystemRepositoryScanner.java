package com.dando.repolens.scanner;

import com.dando.repolens.model.JavaSourceFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// Spring service that scans a repository path and creates a list of JavaSourceFile objects
@Service
public class FileSystemRepositoryScanner implements RepositoryScanner {

    @Override
    public List<JavaSourceFile> scan(Path repositoryPath) throws IOException {

        List<JavaSourceFile> sourceFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(repositoryPath)) {
            List<Path> javaFilePaths = paths
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList();

            // Keep path and content together for each Java source file
            for (Path javaFilePath : javaFilePaths) {
                String content = Files.readString(javaFilePath);
                JavaSourceFile sourceFile = new JavaSourceFile(javaFilePath, content);
                sourceFiles.add(sourceFile);
            }
        }
        return sourceFiles;
    }
}