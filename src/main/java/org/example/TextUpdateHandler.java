package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;

public class TextUpdateHandler implements UpdateHandler {
    private CommunityManager communityManager;
    private SurveyBot bot;

    public TextUpdateHandler(CommunityManager communityManager, SurveyBot bot) {
        this.communityManager = communityManager;
        this.bot = bot;
    }

    @Override
    public void handleUpdate(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();
        String username = firstName + " " + lastName;
        User user = new UserCreator(username, chatId, this.bot).buildUser();
        this.communityManager.processNewUser(user, messageText, this.bot);
    }
}
