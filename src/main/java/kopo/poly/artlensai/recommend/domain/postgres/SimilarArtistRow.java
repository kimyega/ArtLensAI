package kopo.poly.artlensai.recommend.domain.postgres;

public interface SimilarArtistRow {

    Long getArtistId();

    Double getSimilarity();
}
