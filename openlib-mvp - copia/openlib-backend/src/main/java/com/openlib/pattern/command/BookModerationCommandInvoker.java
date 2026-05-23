package com.openlib.pattern.command;

import com.openlib.dto.response.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookModerationCommandInvoker {

    public BookResponse execute(BookModerationCommand command) {
        log.info("Executing book moderation command {}", command.getActionName());
        BookResponse response = command.execute();
        log.info("Completed book moderation command {} for book id={}",
                command.getActionName(), response.id());
        return response;
    }
}
