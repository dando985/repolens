package com.dando.repolens.retrieval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorSimilarityTest {

    @Test
    void identicalVectorsHaveSimilarityOfOne() {
        double[] firstVector = {1.0, 2.0, 3.0};
        double[] secondVector = {1.0, 2.0, 3.0};

        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        assertEquals(1, similarity, 0.000001);
    }

    @Test
    void perpendicularVectorsHaveSimilarityOfZero() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {0.0, 1.0};

        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        assertEquals(0.0, similarity, 0.000001);
    }

    @Test
    void oppositeVectorsHaveSimilarityOfNegativeOne() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {-1.0, 0.0};

        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        assertEquals(-1.0, similarity, 0.000001);
    }
}