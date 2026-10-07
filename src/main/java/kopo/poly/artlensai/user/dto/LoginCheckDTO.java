package kopo.poly.artlensai.user.dto;

import lombok.Builder;

@Builder
public record LoginCheckDTO (
        boolean loggedIn
){
}
