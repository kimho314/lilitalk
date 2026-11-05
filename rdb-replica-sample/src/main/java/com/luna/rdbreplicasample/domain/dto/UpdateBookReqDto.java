package com.luna.rdbreplicasample.domain.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateBookReqDto(
    @NotNull Long id,
    String title,
    String isbn
) {

}
