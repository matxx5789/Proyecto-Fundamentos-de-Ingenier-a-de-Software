package com.openlib.pattern.command;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import com.openlib.dto.response.BookResponse;
import com.openlib.exception.ResourceNotFoundException;
import com.openlib.repository.BookRepository;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class ApproveBookCommand implements BookModerationCommand {

    private final Long bookId;
    private final BookRepository bookRepository;
    private final Function<Book, BookResponse> mapper;

    @Override
    public BookResponse execute() {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", bookId));
        book.setStatus(BookStatus.APPROVED);
        return mapper.apply(bookRepository.save(book));
    }

    @Override
    public String getActionName() {
        return "APPROVE";
    }
}
