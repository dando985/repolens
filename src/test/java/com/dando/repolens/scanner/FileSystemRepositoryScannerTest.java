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

        Files.writeString(temporaryRepository.resolve("Main.java"), """
                public class Main {
                    public static void main(String[] args) {
                    }
                }
                """);

        Files.writeString(temporaryRepository.resolve("README.md"), "# Temporary repository");

        Path serviceDirectory = temporaryRepository.resolve("service");

        Files.createDirectories(serviceDirectory);

        Files.writeString(serviceDirectory.resolve("UserService.java"), """
                public class UserService {
                    public void createUser() {
                    }
                }
                """);

        Files.writeString(serviceDirectory.resolve("settings.json"), "{}");
    }

    @Test
    void findsJavaFilesInNestedDirectories() throws IOException {
        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(temporaryRepository);

        Set<String> fileNames = sourceFiles.stream().map(sourceFile -> sourceFile.getPath().getFileName().toString()).collect(Collectors.toSet());

        assertEquals(2, sourceFiles.size());

        assertEquals(Set.of("Main.java", "UserService.java"), fileNames);
    }

    @Test
    void readsTheJavaFileContent() throws IOException {
        List<JavaSourceFile> sourceFiles = repositoryScanner.scan(temporaryRepository);

        JavaSourceFile userService = sourceFiles.stream().filter(sourceFile -> sourceFile.getPath().getFileName().toString().equals("UserService.java")).findFirst().orElseThrow();

        assertTrue(userService.getContent().contains("createUser"));
    }
}