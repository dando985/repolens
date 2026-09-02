package com.dando.repolens;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {
        // Define path of repository
        Path repositoryPath = Path.of("sample-project").toAbsolutePath().normalize();
        System.out.println("Scanning: " + repositoryPath);
        if (!Files.exists(repositoryPath)) {
            System.out.println("Repository folder was not found.");
            return;
        }

        // Create instances of com.dando.repolens.RepositoryScanner and com.dando.repolens.CodeChunker utility classes
        RepositoryScanner scanner = new RepositoryScanner();
        CodeChunker chunker = new CodeChunker();

        try {
            // Scans repository path for a list of Java source files
            List<JavaSourceFile> sourceFiles = scanner.scan(repositoryPath);
            List<CodeChunk> allChunks = new ArrayList<>();

            // Adds code chunks from all source files to a single list
            for (JavaSourceFile sourceFile : sourceFiles) {
                List<CodeChunk> fileChunks = chunker.createChunks(sourceFile);
                allChunks.addAll(fileChunks);
            }

            System.out.println();
            System.out.println("Indexed " + sourceFiles.size() + " Java file(s).");
            System.out.println("Created " + allChunks.size() + " code chunk(s).");

            // com.dando.repolens.KeywordCodeRetriever uses keyword matching to find relevant code chunks. Initialize retriever with all code chunks.
            //com.dando.repolens.CodeRetriever retriever = new com.dando.repolens.KeywordCodeRetriever(allChunks);

            // Precalculate embeddings for all code chunks and create a semantic index
            EmbeddingProvider embeddingProvider = new OllamaEmbeddingProvider();
            CodeEmbeddingIndexer embeddingIndexer = new CodeEmbeddingIndexer(embeddingProvider);

            // Check if a cached semantic index exists and is valid, otherwise create a new one
            Path indexPath = Path.of(".repolens", "semantic-index.json").toAbsolutePath().normalize();
            SemanticIndexStore indexStore = new SemanticIndexStore();
            Optional<List<EmbeddedCodeChunk>> cachedIndex = indexStore.loadIfValid(indexPath, embeddingProvider.getModelName(), allChunks);
            List<EmbeddedCodeChunk> semanticIndex;
            if (cachedIndex.isPresent()) {
                // Use the cached semantic index if it exists and is valid
                semanticIndex = cachedIndex.get();
                System.out.println();
                System.out.println("Loaded semantic index from cache.");
            } else {
                // Create a new semantic index if no valid cached index exists
                System.out.println();
                System.out.println("Creating semantic index...");
                semanticIndex = embeddingIndexer.createIndex(allChunks);
                try {
                    // Save the newly created semantic index to a JSON file for future use
                    indexStore.save(indexPath, embeddingProvider.getModelName(), semanticIndex);
                    System.out.println("Semantic index created and saved.");
                } catch (IOException exception) {
                    System.out.println("Semantic index created, " + "but could not be saved.");
                    System.out.println(exception.getMessage());
                }
            }

            // Initialize com.dando.repolens.SemanticCodeRetriever with embedding provider and cached semantic index
            CodeRetriever retriever = new SemanticCodeRetriever(embeddingProvider, semanticIndex);

            // Start a loop that prompts the user for search queries and displays the results until the user exits
            try (Scanner console = new Scanner(System.in)) {
                runSearchLoop(console, retriever);
            }

        } catch (IOException exception) {
            System.out.println("Unable to process the repository.");
            System.out.println(exception.getMessage());
        } catch (EmbeddingException exception) {
            System.out.println("Unable to perform semantic search.");
            System.out.println(exception.getMessage());
        }
    }

    // Runs a loop that prompts the user for search queries and displays the results until the user exits
    private static void runSearchLoop(Scanner console, CodeRetriever retriever) {
        while (true) {
            System.out.println();
            System.out.print("Enter a search query " + "(or type 'exit'): ");

            if (!console.hasNextLine()) {
                System.out.println();
                System.out.println("Search ended.");
                return;
            }

            String query = console.nextLine().trim();

            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                System.out.println("Search ended.");
                return;
            }

            if (query.isBlank()) {
                System.out.println("Please enter at least " + "one search word.");
                continue;
            }

            // Create a search query with a maximum of 3 results
            SearchQuery searchQuery = new SearchQuery(query, 3);

            // Perform the search using the retriever and print the results
            List<SearchResult> results = retriever.search(searchQuery);
            printResults(results);
        }
    }

    public static void printResults(List<SearchResult> results) {
        System.out.println();

        if (results.isEmpty()) {
            System.out.println("No matching results were found");
        }

        System.out.println("Found " + results.size() + " matching methods");

        for (SearchResult result : results) {
            CodeChunk chunk = result.getChunk();
            System.out.println();
            System.out.println("------------------------------");
            System.out.printf("Score: %.3f%n", result.getScore());
            System.out.println("File: " + chunk.getFilePath().getFileName());
            System.out.println("Class: " + chunk.getClassName());
            System.out.println("Method: " + chunk.getMethodName());
            System.out.println("Lines: " + chunk.getStartLine() + "-" + chunk.getEndLine());
            System.out.println("------------------------------");
            System.out.println(chunk.getContent());
        }
    }

}