package com.dando.repolens.retrieval;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// Searches code chunks and grades their relevance based on an input search query
public class KeywordCodeRetriever implements CodeRetriever {

    private static final int METHOD_NAME_SCORE = 3;
    private static final int CLASS_NAME_SCORE = 2;
    private static final int CONTENT_SCORE = 1;

    private final List<CodeChunk> chunks;

    public KeywordCodeRetriever(List<CodeChunk> chunks) {
        this.chunks = List.copyOf(chunks);
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
                score += METHOD_NAME_SCORE;
            }
            if (className.contains(keyword)) {
                score += CLASS_NAME_SCORE;
            }
            if (content.contains(keyword)) {
                score += CONTENT_SCORE;
            }
        }

        return score;
    }
}