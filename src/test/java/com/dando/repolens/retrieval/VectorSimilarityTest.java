package com.dando.repolens.retrieval;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VectorSimilarityTest {

    private static final double TOLERANCE = 0.000001;

    @Test
    void identicalVectorsHaveSimilarityOfOne() {
        double[] firstVector = {1.0, 2.0, 3.0};
        double[] secondVector = {1.0, 2.0, 3.0};
        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        // Check that identical vectors have a cosine similarity of 1.0
        assertEquals(1.0, similarity, TOLERANCE);
    }

    @Test
    void perpendicularVectorsHaveSimilarityOfZero() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {0.0, 1.0};
        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        // Check that perpendicular vectors have a cosine similarity of 0.0
        assertEquals(0.0, similarity, TOLERANCE);
    }

    @Test
    void oppositeVectorsHaveSimilarityOfNegativeOne() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {-1.0, 0.0};

        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        // Check that opposite vectors have a cosine similarity of -1.0
        assertEquals(-1.0, similarity, TOLERANCE);
    }

    @Test
    void partiallyAlignedVectorsHaveExpectedSimilarity() {
        double[] firstVector = {1.0, 0.0};
        double[] secondVector = {1.0, 1.0};

        // Calculate the expected cosine similarity using the formula: (A · B) / (||A|| * ||B||)
        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);
        double dotProduct = firstVector[0] * secondVector[0] + firstVector[1] * secondVector[1];
        double firstMagnitude = Math.sqrt(firstVector[0] * firstVector[0] + firstVector[1] * firstVector[1]);
        double secondMagnitude = Math.sqrt(secondVector[0] * secondVector[0] + secondVector[1] * secondVector[1]);
        double expected = dotProduct / (firstMagnitude * secondMagnitude);

        // Check that partially aligned vectors have the expected cosine similarity
        assertEquals(expected, similarity, TOLERANCE);
    }

    @Test
    void zeroVectorHasSimilarityOfZero() {
        double[] firstVector = {0.0, 0.0};
        double[] secondVector = {1.0, 0.0};
        double similarity = VectorSimilarity.cosineSimilarity(firstVector, secondVector);

        // Check that a zero vector has a cosine similarity of 0.0 with any other vector
        assertEquals(0.0, similarity, TOLERANCE);
    }

    @Test
    void rejectsNullVectors() {
        // Check that a null vector input throws an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> VectorSimilarity.cosineSimilarity(null, new double[]{1.0}));
        assertThrows(IllegalArgumentException.class, () -> VectorSimilarity.cosineSimilarity(new double[]{1.0}, null));
    }

    @Test
    void rejectsEmptyVectors() {
        // Check that an empty vector input throws an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> VectorSimilarity.cosineSimilarity(new double[]{}, new double[]{}));
    }

    @Test
    void rejectsVectorsWithDifferentLengths() {
        // Check that vectors with different lengths throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> VectorSimilarity.cosineSimilarity(new double[]{1.0, 2.0}, new double[]{1.0}));
    }
}