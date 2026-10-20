package com.luna.rdbreplicasample.controller;

import com.luna.rdbreplicasample.domain.dto.CreateBookReqDto;
import com.luna.rdbreplicasample.domain.dto.GetBookRespDto;
import com.luna.rdbreplicasample.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Controller
@RequestMapping("/api/books")
public class BookController {

    private static final Logger log = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/")
    public ResponseEntity<Void> createBook(@RequestBody CreateBookReqDto request) {
        bookService.create(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetBookRespDto> getBook(@PathVariable(value = "id") Long id) {
        GetBookRespDto resp = bookService.getById(id);
        return ResponseEntity.ok(resp);
    }
}
