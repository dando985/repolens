public class SearchResult {

    private final CodeChunk chunk;
    private final int score;

    public SearchResult(CodeChunk chunk, int score) {
        this.chunk = chunk;
        this.score = score;
    }

    public CodeChunk getChunk() {
        return chunk;
    }

    public int getScore() {
        return score;
    }
}