package com.luna.rdbreplicasample.domain.dto;

import jakarta.validation.constraints.NotNull;

public record GetBookReqDto(
    @NotNull Long id
) {

}
