import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        try {
            List<JavaSourceFile> sourceFiles = scanner.scan(repositoryPath);
            List<CodeChunk> allChunks = new ArrayList<>();

            for (JavaSourceFile sourceFile : sourceFiles) {
                List<CodeChunk> fileChunks = chunker.createChunks(sourceFile, 5);
                allChunks.addAll(fileChunks);
            }

            System.out.println();
            System.out.println(
                    "Indexed " + sourceFiles.size() + " Java file(s)."
            );

            System.out.println(
                    "Created " + allChunks.size() + " code chunk(s)."
            );

            for (CodeChunk chunk : allChunks) {
                System.out.println();
                System.out.println("------------------------------");
                System.out.println("File: " + chunk.getFilePath());
                System.out.println(
                        "Lines: "
                                + chunk.getStartLine()
                                + "-"
                                + chunk.getEndLine()
                );
                System.out.println("------------------------------");
                System.out.println(chunk.getContent());
            }
        } catch (IOException exception) {
            System.out.println("Unable to scan the repository.");
            System.out.println(exception.getMessage());
        }
    }
}