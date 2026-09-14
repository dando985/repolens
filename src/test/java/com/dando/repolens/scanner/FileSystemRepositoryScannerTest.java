package com.dando.repolens.scanner;

import com.dando.repolens.model.JavaSourceFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSystemRepositoryScannerTest {

    @TempDir
    Path temporaryRepository;

    private RepositoryScanner repositoryScanner;

    @BeforeEach
    void setUp() throws IOException {
        repositoryScanner = new FileSystemRepositoryScanner();

        // Create a temporary repository structure for testing
        Files.writeString(temporaryRepository.resolve("Main.java"),
                """
                public class Main {
                    public static void main(String[] args) {
                    }
                }
                """
        );

        Files.writeString(temporaryRepository.resolve("README.md"),
                "# Temporary repository"
        );

        Path serviceDirectory = temporaryRepository.resolve("service");
        Files.createDirectories(serviceDirectory);
        Files.writeString(serviceDirectory.resolve("UserService.java"),
                """
                public class UserService {
                    public void createUser() {
                    }
                }
                """
        );

        Files.writeString(serviceDirectory.resolve("settings.json"),
                "{}"
        );
    }

    @Test
    void findsOnlyJavaFilesInNestedDirectories() throws IOException {
        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(temporaryRepository);

        // Get the relative paths of the found Java files for easier comparison
        Set<Path> relativePaths = sourceFiles.stream()
                .map(JavaSourceFile::getPath)
                .map(temporaryRepository::relativize)
                .collect(Collectors.toSet());

        // Check that only 2 Java files are found: Main.java and service/UserService.java
        assertEquals(2, sourceFiles.size());

        // Check that the relative paths of the found Java files are as expected
        assertEquals(Set.of(Path.of("Main.java"), Path.of("service", "UserService.java")), relativePaths);
    }

    @Test
    void readsJavaFileContent() throws IOException {
        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(temporaryRepository);

        // Find the UserService.java file in the scanned source files
        JavaSourceFile userService = sourceFiles.stream()
                .filter(sourceFile ->
                        sourceFile
                                .getPath()
                                .getFileName()
                                .toString()
                                .equals("UserService.java")
                )
                .findFirst()
                .orElseThrow();

        // Check that the content of UserService.java contains the expected method name
        assertTrue(userService.getContent().contains("createUser"));
    }

    @Test
    void ignoresDirectoriesWhoseNamesEndWithJava() throws IOException {
        Files.createDirectories(temporaryRepository.resolve("Generated.java"));

        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(temporaryRepository);

        // Check that the scanner finds only 2 Java source code files, ignoring the Generated.java directory
        assertEquals(2, sourceFiles.size());
    }
}