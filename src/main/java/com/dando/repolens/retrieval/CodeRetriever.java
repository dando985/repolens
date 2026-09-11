package com.dando.repolens.retrieval;

import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;

import java.util.List;

public interface CodeRetriever {

    /**
     * Returns up to the requested number of matches,
     * ordered from highest to lowest score.
     */
    List<SearchResult> search(SearchQuery searchQuery);
}