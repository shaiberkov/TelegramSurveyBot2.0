package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;

public class TextUpdateHandler implements UpdateHandler {
    private CommunityManager communityManager;
    private SurveyBot bot; // הוספת הבוט

    public TextUpdateHandler(CommunityManager communityManager, SurveyBot bot) {
        this.communityManager = communityManager;
        this.bot = bot; // שמירת הבוט כפרמטר של המחלקה
    }

    @Override
    public void handleUpdate(Update update) {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();
        String lastName = update.getMessage().getFrom().getLastName();
        String username = firstName + " " + lastName;
        User user = new UserCreator(username, chatId, bot).buildUser(); // העברת הבוט ל-UserCreator
        this.communityManager.processNewUser(user, messageText, bot); // העברת הבוט ל-processNewUser
    }
}
