package kopo.poly.artlensai.artist.domain.postgres.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "artist_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtistImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "img_id")
    private Long imgId;

    @Column(name = "artist_id")
    private Long artistId;

    @Column(name = "img_url")
    private String imgUrl;

    @Column(name = "img_key")
    private String imgKey;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "safebooru_id")
    private Long safebooruId;
}