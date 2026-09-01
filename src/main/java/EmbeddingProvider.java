public interface EmbeddingProvider {

    String getModelName();

    double[] createEmbedding(String text);
}