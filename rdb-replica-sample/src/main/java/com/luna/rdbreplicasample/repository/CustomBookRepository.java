package com.luna.rdbreplicasample.repository;

import com.luna.rdbreplicasample.domain.dto.UpdateBookReqDto;
import com.luna.rdbreplicasample.domain.model.Book;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CustomBookRepository {

    private final JdbcClient jdbcClient;

    public CustomBookRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public void update(Book originalBook, UpdateBookReqDto request) {
        List<String> sets = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("id", request.id());
        if (request.title() != null && !request.title().isBlank()) {
            sets.add("title = :title");
            params.put("title", request.title());
        }
        if (request.isbn() != null && !request.isbn().isBlank()) {
            sets.add("isbn = :isbn");
            params.put("isbn", request.isbn());
        }

        if (sets.isEmpty()) {
            return;
        }

        String sql = "UPDATE book SET " + String.join(", ", sets) + " WHERE book_id = :id";
        jdbcClient.sql(sql).params(params).update();
    }
}
