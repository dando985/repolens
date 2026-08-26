import java.util.ArrayList;
import java.util.List;

public class CodeSearchService {

    public List<SearchResult> search(List<CodeChunk> chunks, String query) {
        List<SearchResult> results = new ArrayList<>();

        String[] keywords = query.toLowerCase().trim().split("\\s+");

        for (CodeChunk chunk : chunks) {
            int score = calculateScore(chunk, keywords);

            if (score > 0) {
                results.add(new SearchResult(chunk, score));
            }
        }

        results.sort(
                (first, second) ->
                        Integer.compare(
                                second.getScore(),
                                first.getScore()
                        )
        );

        return results;
    }

    private int calculateScore(
            CodeChunk chunk,
            String[] keywords
    ) {
        int score = 0;

        String className =
                chunk.getClassName().toLowerCase();

        String methodName =
                chunk.getMethodName().toLowerCase();

        String content =
                chunk.getContent().toLowerCase();

        for (String keyword : keywords) {
            if (keyword.isBlank()) {
                continue;
            }

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