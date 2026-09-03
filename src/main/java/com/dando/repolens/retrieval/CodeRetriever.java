package com.dando.repolens.retrieval;

import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;

import java.util.List;

public interface CodeRetriever {

    List<SearchResult> search(SearchQuery searchQuery);
}