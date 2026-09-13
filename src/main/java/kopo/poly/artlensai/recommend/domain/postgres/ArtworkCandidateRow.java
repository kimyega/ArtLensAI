package kopo.poly.artlensai.recommend.domain.postgres;

public interface ArtworkCandidateRow {

    Long getImgId();

    Long getArtistId();

    String getImgUrl();
}
