import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        try {
            List<JavaSourceFile> sourceFiles = scanner.scan(repositoryPath);

            System.out.println();
            System.out.println("Indexed " + sourceFiles.size() + " Java file(s).");

            for (JavaSourceFile sourceFile : sourceFiles) {
                System.out.println();
                System.out.println(
                        "--- " + sourceFile.getFileName() + " ---"
                );
                System.out.println("Path: " + sourceFile.getPath());
                System.out.println();
                System.out.println(sourceFile.getContent());
            }
        } catch (IOException exception) {
            System.out.println("Unable to scan the repository.");
            System.out.println(exception.getMessage());
        }
    }
}