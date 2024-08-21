package org.example;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
    public class UserCreator {
    private String username;
    private Long chatId;
    private TelegramLongPollingBot bot;


    public User buildUser (){
        return new User(this.username, this.chatId, this.bot);
    }
}

