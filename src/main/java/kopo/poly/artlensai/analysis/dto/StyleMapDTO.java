package kopo.poly.artlensai.analysis.dto;

import lombok.Builder;

@Builder(toBuilder = true)
public record StyleMapDTO(

        Double x,

        Double y,

        String clusterLabel

) {
}
