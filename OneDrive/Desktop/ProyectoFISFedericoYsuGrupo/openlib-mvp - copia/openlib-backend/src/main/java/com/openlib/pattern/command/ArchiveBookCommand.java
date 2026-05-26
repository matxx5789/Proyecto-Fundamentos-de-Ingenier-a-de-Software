package com.openlib.pattern.command;

import com.openlib.domain.Book;
import com.openlib.domain.BookStatus;
import com.openlib.dto.response.BookResponse;
import com.openlib.repository.BookRepository;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
public class ArchiveBookCommand implements BookModerationCommand {

    private final Book book;
    private final BookRepository bookRepository;
    private final Function<Book, BookResponse> mapper;

    @Override
    public BookResponse execute() {
        book.setStatus(BookStatus.ARCHIVED);
        return mapper.apply(bookRepository.save(book));
    }

    @Override
    public String getActionName() {
        return "ARCHIVE";
    }
}
