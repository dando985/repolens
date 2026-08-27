import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Path repositoryPath = Path.of("sample-project").toAbsolutePath().normalize();

        System.out.println("Scanning: " + repositoryPath);

        if (!Files.exists(repositoryPath)) {
            System.out.println("Repository folder was not found.");
            return;
        }

        RepositoryScanner scanner = new RepositoryScanner();
        CodeChunker chunker = new CodeChunker();
        CodeSearchService searchService = new CodeSearchService();

        try {
            List<JavaSourceFile> sourceFiles = scanner.scan(repositoryPath);
            List<CodeChunk> allChunks = new ArrayList<>();

            for (JavaSourceFile sourceFile : sourceFiles) {
                List<CodeChunk> fileChunks = chunker.createChunks(sourceFile);
                allChunks.addAll(fileChunks);
            }

            System.out.println();
            System.out.println(
                    "Indexed " + sourceFiles.size() + " Java file(s)."
            );

            System.out.println(
                    "Created " + allChunks.size() + " code chunk(s)."
            );

            Scanner console = new Scanner(System.in);
            System.out.println();
            System.out.print("Enter a search query: ");
            String query = console.nextLine();

            if (query.isBlank()) {
                System.out.println("Please enter at least one search word.");
                return;
            }

            int maxResults = 3;
            List<SearchResult> results = searchService.search(allChunks, query, maxResults);
            printResults(results);
        } catch (IOException exception) {
            System.out.println("Unable to scan the repository.");
            System.out.println(exception.getMessage());
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
            System.out.println("Score: " + result.getScore());

            System.out.println("File: " + chunk.getFilePath().getFileName());
            System.out.println("Class: " + chunk.getClassName());
            System.out.println("Method: " + chunk.getMethodName());
            System.out.println(
                    "Lines: "
                            + chunk.getStartLine()
                            + "-"
                            + chunk.getEndLine()
            );
            System.out.println("------------------------------");
            System.out.println(chunk.getContent());
        }
    }
}