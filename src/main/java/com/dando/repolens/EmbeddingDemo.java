package com.dando.repolens;

public class EmbeddingDemo {

    public static void main(String[] args) {
        try {
            EmbeddingProvider provider = new OllamaEmbeddingProvider();

            double[] embedding = provider.createEmbedding("authenticate a user");

            System.out.println("Embedding dimensions: " + embedding.length);
            System.out.println("First five values:");
            int valuesToPrint = Math.min(5, embedding.length);

            for (int index = 0; index < valuesToPrint; index++) {
                System.out.printf("%d: %.6f%n", index, embedding[index]);
            }

        } catch (EmbeddingException exception) {
            System.out.println("Unable to create the embedding.");
            System.out.println(exception.getMessage());
        }
    }
}