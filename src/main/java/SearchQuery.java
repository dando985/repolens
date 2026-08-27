public class SearchQuery {

    private final String text;
    private final int maxResults;

    public SearchQuery(String text, int maxResults) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Search text cannot be empty.");
        }

        if (maxResults <= 0) {
            throw new IllegalArgumentException("Maximum results must be greater than zero.");
        }

        this.text = text.trim();
        this.maxResults = maxResults;
    }

    public String getText() {
        return text;
    }

    public int getMaxResults() {
        return maxResults;
    }
}