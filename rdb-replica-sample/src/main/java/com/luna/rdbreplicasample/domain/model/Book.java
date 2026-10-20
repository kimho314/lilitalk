package com.luna.rdbreplicasample.domain.model;

import jakarta.validation.constraints.Max;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "book")
public record Book(
    @Id @Column(value = "book_id") Long id,
    @Column(value = "title") @NonNull @Max(value = 300) String title,
    @Column(value = "isbn") @NonNull @Max(value = 30) String isbn
) {

    public Book {
    }

    public Book(@NonNull String title, @NonNull String isbn) {
        this(null, title, isbn);
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
