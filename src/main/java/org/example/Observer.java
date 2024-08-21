package org.example;

import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;

public interface Observer {
    void update(String message);
    Long getChatId();


}

