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
        List<CodeChunk> chunks = List.of(createChunk("UserService", "findUser", """
                public User findUser(String id) {
                    return users.get(id);
                }
                """), createChunk("UserService", "deleteAccount", """
                public void deleteAccount(String id) {
                    accounts.remove(id);
                }
                """), createChunk("NotificationService", "sendEmail", """
                public void sendEmail(User user) {
                    emailClient.send(user.getEmail());
                }
                """), createChunk("OrderService", "calculateTotal", """
                public double calculateTotal(Order order) {
                    return order.getTotal();
                }
                """));

        retriever = new KeywordCodeRetriever(chunks);
    }

    @Test
    void ranksStrongerMatchesBeforeWeakerMatches() {
        SearchQuery query = new SearchQuery("user", 10);

        List<SearchResult> results = retriever.search(query);

        assertEquals(3, results.size());

        assertEquals("findUser", results.get(0).getChunk().getMethodName());

        assertEquals("deleteAccount", results.get(1).getChunk().getMethodName());

        assertEquals("sendEmail", results.get(2).getChunk().getMethodName());

        assertTrue(results.get(0).getScore() > results.get(1).getScore());

        assertTrue(results.get(1).getScore() > results.get(2).getScore());
    }

    @Test
    void respectsTheRequestedResultLimit() {
        SearchQuery query = new SearchQuery("user", 2);

        List<SearchResult> results = retriever.search(query);

        assertEquals(2, results.size());
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        SearchQuery query = new SearchQuery("inventory", 5);

        List<SearchResult> results = retriever.search(query);

        assertTrue(results.isEmpty());
    }

    private CodeChunk createChunk(String className, String methodName, String content) {
        return new CodeChunk(Path.of(className + ".java"), className, methodName, 1, 3, content);
    }
}