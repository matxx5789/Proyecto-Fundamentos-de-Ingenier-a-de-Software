package com.openlib.pattern.command;

import com.openlib.dto.response.BookResponse;

public interface BookModerationCommand {

    BookResponse execute();

    String getActionName();
}
