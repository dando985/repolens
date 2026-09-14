package com.dando.repolens.retrieval;

import com.dando.repolens.model.CodeChunk;
import com.dando.repolens.model.SearchQuery;
import com.dando.repolens.model.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeywordCodeRetrieverTest {

    private KeywordCodeRetriever retriever;

    @BeforeEach
    void setUp() {
        List<CodeChunk> chunks = List.of(
                createChunk(
                        "NotificationService",
                        "sendEmail",
                        """
                        public void sendEmail(User user) {
                            emailClient.send(user.getEmail());
                        }
                        """
                ),
                createChunk(
                        "UserService",
                        "deleteAccount",
                        """
                        public void deleteAccount(String id) {
                            accounts.remove(id);
                        }
                        """
                ),
                createChunk(
                        "OrderService",
                        "calculateTotal",
                        """
                        public double calculateTotal(Order order) {
                            return order.getTotal();
                        }
                        """
                ),
                createChunk(
                        "UserService",
                        "findUser",
                        """
                        public User findUser(String id) {
                            return users.get(id);
                        }
                        """
                )
        );

        retriever = new KeywordCodeRetriever(chunks);
    }

    @Test
    void ranksStrongerMatchesBeforeWeakerMatches() {
        SearchQuery query = new SearchQuery("user", 10);
        List<SearchResult> results = retriever.search(query);

        // Check that only positive non-zero scores are returned
        assertEquals(3, results.size());

        // Check that the results are sorted by score in descending order and their corresponding scores are correct
        // Top score
        assertEquals("findUser", results.get(0).getChunk().getMethodName());
        assertEquals(6.0, results.get(0).getScore());

        // Second top score
        assertEquals("deleteAccount", results.get(1).getChunk().getMethodName());
        assertEquals(2.0, results.get(1).getScore());

        // Third top score
        assertEquals("sendEmail", results.get(2).getChunk().getMethodName());
        assertEquals(1.0, results.get(2).getScore());
    }

    @Test
    void respectsRequestedResultLimit() {
        SearchQuery query = new SearchQuery("user", 2);
        List<SearchResult> results = retriever.search(query);

        // Check that only the requested number of results are returned
        assertEquals(2, results.size());

        // Verify the order of the results based on their scores
        assertEquals("findUser", results.get(0).getChunk().getMethodName());
        assertEquals("deleteAccount", results.get(1).getChunk().getMethodName());
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        SearchQuery query = new SearchQuery("inventory", 5);
        List<SearchResult> results = retriever.search(query);

        // Check that an empty list is returned when no matches are found
        assertTrue(results.isEmpty());
    }

    private CodeChunk createChunk(String className, String methodName, String content) {
        return new CodeChunk(
                Path.of(className + ".java"),
                className,
                methodName,
                1,
                3,
                content
        );
    }
}