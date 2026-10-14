// ArtistStyleProfileDocument.java

package kopo.poly.artlensai.artist.domain.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Document(
        collection =
                "artist_style_profiles"
)
public record ArtistStyleProfileDocument(

        @Id
        String id,

        Long artistId,

        String artistName,

        String profileImg,

        String styleDesc,

        List<String> tags,

        List<String> colors,

        LocalDateTime updatedAt

) {
}
