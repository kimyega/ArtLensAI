package kopo.poly.artlensai.recommend.util;

import java.util.List;

public final class VectorMath {

    private VectorMath() {
    }

    public static float[] average(List<float[]> vectors) {

        if (vectors.isEmpty()) {

            throw new IllegalArgumentException("vectors must not be empty");
        }

        int dim = vectors.get(0).length;
        float[] sum = new float[dim];

        for (float[] v : vectors) {

            for (int i = 0; i < dim; i++) {

                sum[i] += v[i];
            }
        }

        for (int i = 0; i < dim; i++) {

            sum[i] /= vectors.size();
        }

        return sum;
    }

    public static double cosineSimilarity(float[] a, float[] b) {

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {

            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {

            return 0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static String toPgVectorLiteral(float[] vector) {

        StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < vector.length; i++) {

            if (i > 0) {

                sb.append(',');
            }

            sb.append(vector[i]);
        }

        return sb.append(']').toString();
    }
}
