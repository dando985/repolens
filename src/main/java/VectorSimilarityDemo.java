public class VectorSimilarityDemo {

    public static void main(String[] args) {
        double[] queryVector = {1.0, 0.0};

        double[] similarVector = {0.9, 0.1};

        double[] differentVector = {0.0, 1.0};

        double similarScore = VectorSimilarity.cosineSimilarity(queryVector, similarVector);

        double differentScore = VectorSimilarity.cosineSimilarity(queryVector, differentVector);

        System.out.printf("Similar vector score: %.3f%n", similarScore);

        System.out.printf("Different vector score: %.3f%n", differentScore);
    }
}