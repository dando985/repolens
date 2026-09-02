package com.dando.repolens;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Searches code chunks and grades their relevance based on an input search query
public class KeywordCodeRetriever implements CodeRetriever {

    private final List<CodeChunk> chunks;

    public KeywordCodeRetriever(List<CodeChunk> chunks) {
        this.chunks = new ArrayList<>(Objects.requireNonNull(chunks));
    }

    @Override
    public List<SearchResult> search(SearchQuery searchQuery) {
        List<SearchResult> results = new ArrayList<>();

        String[] keywords = searchQuery.getText().toLowerCase(Locale.ROOT).trim().split("\\s+");

        // Score each code chunk by their relevance
        for (CodeChunk chunk : chunks) {
            int score = calculateScore(chunk, keywords);

            if (score > 0) {
                results.add(new SearchResult(chunk, score));
            }
        }

        // Sorts relevant code chunks by their score in descending order
        results.sort((first, second) -> Double.compare(second.getScore(), first.getScore()));
        // Determine result count based on result size or a set max result size (whichever is lower)
        int resultCount = Math.min(results.size(), searchQuery.getMaxResults());

        return new ArrayList<>(results.subList(0, resultCount));
    }

    /*
     * Calculate score of code chunk by its matching to keywords
     * Methods = +3 points
     * Class = +2 points
     * Body = +1 point
      */
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