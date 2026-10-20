package com.luna.rdbreplicasample.domain.dto;

import jakarta.validation.constraints.NotNull;

public record CreateBookReqDto(
    @NotNull String title,
    @NotNull String isbn
) {

}
