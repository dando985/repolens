package com.dando.repolens.retrieval;

// Utility class to compare two vectors and return cosine similarity
public final class VectorSimilarity {

    private VectorSimilarity() {
    }

    public static double cosineSimilarity(double[] firstVector, double[] secondVector) {
        if (firstVector == null || secondVector == null) {
            throw new IllegalArgumentException("Vectors cannot be null.");
        }

        if (firstVector.length == 0 || secondVector.length == 0) {
            throw new IllegalArgumentException("Vectors cannot be empty.");
        }

        if (firstVector.length != secondVector.length) {
            throw new IllegalArgumentException("Vectors must have the same length.");
        }

        double dotProduct = 0.0;
        double firstMagnitudeSquared = 0.0;
        double secondMagnitudeSquared = 0.0;

        // Calculate dot product and vector magnitudes squared for each vector
        for (int index = 0; index < firstVector.length; index++) {
            dotProduct += firstVector[index] * secondVector[index];

            firstMagnitudeSquared += firstVector[index] * firstVector[index];
            secondMagnitudeSquared += secondVector[index] * secondVector[index];
        }

        // Handle zero vectors to avoid dividing by zero (zero similarity)
        if (firstMagnitudeSquared == 0.0 || secondMagnitudeSquared == 0.0) {
            return 0.0;
        }

        // Calculate vector magnitude
        double magnitudeProduct = Math.sqrt(firstMagnitudeSquared) * Math.sqrt(secondMagnitudeSquared);

        // Calculate and return cosine similarity
        return dotProduct / magnitudeProduct;
    }
}