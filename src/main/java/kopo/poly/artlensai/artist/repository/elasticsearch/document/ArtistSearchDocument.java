// ArtistSearchDocument.java

package kopo.poly.artlensai.artist.repository.elasticsearch.document;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.List;

@Builder
@Document(indexName = "artist_style_profiles")
public record ArtistSearchDocument(

        @Id
        String id,

        Long artistId,

        @MultiField(
                mainField = @Field(type = FieldType.Text),
                otherFields = {
                        @InnerField(suffix = "raw", type = FieldType.Keyword)
                }
        )
        String artistName,

        @Field(type = FieldType.Keyword, index = false)
        String profileImg,

        @Field(type = FieldType.Text)
        String styleDesc,

        @Field(type = FieldType.Keyword)
        List<String> tags,

        @Field(type = FieldType.Keyword, index = false)
        List<String> colors

) {
}
