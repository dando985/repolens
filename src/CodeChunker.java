import java.util.ArrayList;
import java.util.List;

public class CodeChunker {

    public List<CodeChunk> createChunks(JavaSourceFile sourceFile, int linesPerChunk) {
        List<CodeChunk> chunks = new ArrayList<>();
        String[] lines = sourceFile.getContent().split("\\R");

        for (int startIndex = 0; startIndex < lines.length; startIndex += linesPerChunk) {
            int endIndex = Math.min(startIndex + linesPerChunk, lines.length);
            StringBuilder chunkContent = new StringBuilder();

            for (int lineIndex = startIndex; lineIndex < endIndex; lineIndex++) {
                chunkContent.append(lines[lineIndex]);
                if (lineIndex < endIndex - 1) {
                    chunkContent.append(System.lineSeparator());
                }
            }

            CodeChunk chunk = new CodeChunk(
                    sourceFile.getPath(),
                    startIndex + 1,
                    endIndex,
                    chunkContent.toString()
            );
            chunks.add(chunk);
        }
        return chunks;
    }
}