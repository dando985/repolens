import java.util.List;

public interface CodeRetriever {

    List<SearchResult> search(List<CodeChunk> chunks, SearchQuery searchQuery);
}