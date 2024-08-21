package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class User implements Observer {
    private String username;
    private Long chatId; // מזהה ה-chat של המשתמש בטלגרם
    private TelegramLongPollingBot bot;

    @Override
    public void update(String message) {
        Utils.sendMessageToUser(this.chatId, message,this.bot);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(chatId, user.chatId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }

}