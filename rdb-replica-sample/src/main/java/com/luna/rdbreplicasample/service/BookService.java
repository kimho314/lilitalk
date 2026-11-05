package com.luna.rdbreplicasample.service;

import com.luna.rdbreplicasample.domain.dto.CreateBookReqDto;
import com.luna.rdbreplicasample.domain.dto.GetBookRespDto;
import com.luna.rdbreplicasample.domain.dto.UpdateBookReqDto;
import com.luna.rdbreplicasample.domain.model.Book;
import com.luna.rdbreplicasample.exceptions.NoBookException;
import com.luna.rdbreplicasample.repository.BookRepository;
import com.luna.rdbreplicasample.repository.CustomBookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;
    private final CustomBookRepository customBookRepository;

    public BookService(
        BookRepository bookRepository,
        CustomBookRepository customBookRepository
    ) {
        this.bookRepository = bookRepository;
        this.customBookRepository = customBookRepository;
    }

    @Transactional
    public void create(CreateBookReqDto request) {
        Book book = new Book(request.title(), request.isbn());
        bookRepository.save(book);
    }

    @CacheEvict(value = "book", key = "#request.id")
    @Transactional
    public void update(UpdateBookReqDto request) {
        Book foundBook = bookRepository.findById(request.id())
            .orElseThrow(() -> new NoBookException(request.id()));
        customBookRepository.update(foundBook, request);
    }

    @Cacheable(value = "book", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public GetBookRespDto getById(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new NoBookException(id));
        return GetBookRespDto.of(book);
    }
}
