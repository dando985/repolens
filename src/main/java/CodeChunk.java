import java.nio.file.Path;

public class CodeChunk {

    private final Path filePath;
    private final int startLine;
    private final int endLine;
    private final String content;

    public CodeChunk(Path filePath, int startLine, int endLine, String content) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.content = content;
    }

    public Path getFilePath() {
        return filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getContent() {
        return content;
    }
}