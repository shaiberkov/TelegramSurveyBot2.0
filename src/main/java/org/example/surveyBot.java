package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.polls.Poll;
import org.telegram.telegrambots.meta.api.objects.polls.PollAnswer;
import org.telegram.telegrambots.meta.api.objects.polls.PollOption;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.*;

public class surveyBot extends TelegramLongPollingBot {
    private CommunityManager communityManager;
    private SurveyCreatorManager surveyCreatorManager;
    private SurveyStatisticsManager surveyStatisticsManager;
    private BotConfig botConfig;
    public surveyBot() {
        this.botConfig = new BotConfig();
        this.communityManager = new CommunityManager();
        this.surveyCreatorManager = new SurveyCreatorManager(communityManager);
        this.surveyStatisticsManager = new SurveyStatisticsManager(surveyCreatorManager.getPollsMap(), communityManager);
    }

    @Override
    public String getBotUsername() {
        return "Shai2024";
    }

    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        handlingTextResponses(update);
        handlingSurveyResponses(update);
    }

    private void handlingTextResponses(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String username = update.getMessage().getFrom().getFirstName()+" "+update.getMessage().getFrom().getLastName();
            User user = new UserCreator(username, chatId, this).buildUser();
            this.communityManager.processNewUser(user, messageText, this);
        }
    }

    private void handlingSurveyResponses(Update update) {
        if (update.hasPollAnswer()) {
            PollAnswer pollAnswer = update.getPollAnswer();
            String pollId = pollAnswer.getPollId();
            SurveyDetails surveyDetails = this.surveyCreatorManager.getSurveyDetailsByPollId(pollId);
            if (surveyDetails != null) {
                Integer selectedOptionId = pollAnswer.getOptionIds().get(0);
                String selectedOption = surveyDetails.getOptions().get(selectedOptionId);
                Long userID = pollAnswer.getUser().getId();
                String question = surveyDetails.getQuestion();
                this.surveyStatisticsManager.recordAnswer(question, selectedOption, userID);
            } else {
                System.out.println("Poll not found for ID: " + pollId);
            }
        }
    }
}











