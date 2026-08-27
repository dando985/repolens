import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CodeSearchService {

    public List<SearchResult> search(List<CodeChunk> chunks, String query, int maxResults) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        if (maxResults < 1) {
            return new ArrayList<>();
        }

        List<SearchResult> results = new ArrayList<>();

        String[] keywords = query.toLowerCase(Locale.ROOT).trim().split("\\s+");

        for (CodeChunk chunk : chunks) {
            int score = calculateScore(chunk, keywords);

            if (score > 0) {
                results.add(new SearchResult(chunk, score));
            }
        }

        results.sort((first, second) -> Integer.compare(second.getScore(), first.getScore()));
        int resultCount = Math.min(results.size(), maxResults);

        return new ArrayList<>(results.subList(0, resultCount));
    }

    private int calculateScore(CodeChunk chunk, String[] keywords) {
        int score = 0;

        String className = chunk.getClassName().toLowerCase(Locale.ROOT);
        String methodName = chunk.getMethodName().toLowerCase(Locale.ROOT);
        String content = chunk.getContent().toLowerCase(Locale.ROOT);

        for (String keyword : keywords) {
            if (methodName.contains(keyword)) {
                score += 3;
            }
            if (className.contains(keyword)) {
                score += 2;
            }
            if (content.contains(keyword)) {
                score += 1;
            }
        }

        return score;
    }
}