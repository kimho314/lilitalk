package com.luna.rdbreplicasample.service;

import com.luna.rdbreplicasample.domain.dto.CreateBookReqDto;
import com.luna.rdbreplicasample.domain.dto.GetBookRespDto;
import com.luna.rdbreplicasample.domain.model.Book;
import com.luna.rdbreplicasample.exceptions.NoBookException;
import com.luna.rdbreplicasample.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void create(CreateBookReqDto request) {
        Book book = new Book(request.title(), request.isbn());
        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public GetBookRespDto getById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new NoBookException(id));
        return GetBookRespDto.of(book);
    }
}
