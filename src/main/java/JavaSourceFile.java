import java.nio.file.Path;

public class JavaSourceFile {

    private final Path path;
    private final String content;

    public JavaSourceFile(Path path, String content) {
        this.path = path;
        this.content = content;
    }

    public Path getPath() {
        return path;
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    public String getContent() {
        return content;
    }
}