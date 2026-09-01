import java.util.List;

public interface CodeRetriever {

    List<SearchResult> search(SearchQuery searchQuery);
}