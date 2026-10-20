package com.luna.rdbreplicasample.domain.dto;

import com.luna.rdbreplicasample.domain.model.Book;

public record GetBookRespDto(
    Long id,
    String title,
    String isbn
) {

    public static GetBookRespDto of(Book book) {
        return new GetBookRespDto(
            book.id(),
            book.title(),
            book.isbn()
        );
    }
}
