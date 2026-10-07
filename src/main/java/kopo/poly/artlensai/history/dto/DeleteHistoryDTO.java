package kopo.poly.artlensai.history.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DeleteHistoryDTO(

        List<Long> ids

) {
}
