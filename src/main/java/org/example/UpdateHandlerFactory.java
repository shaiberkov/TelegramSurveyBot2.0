package org.example;

import org.telegram.telegrambots.meta.api.objects.Update;

public class UpdateHandlerFactory {
    private CommunityManager communityManager;
    private SurveyCreatorManager surveyCreatorManager;
    private SurveyStatisticsManager surveyStatisticsManager;
    private SurveyBot bot;

    public UpdateHandlerFactory(CommunityManager communityManager, SurveyCreatorManager surveyCreatorManager, SurveyStatisticsManager surveyStatisticsManager, SurveyBot bot) {
        this.communityManager = communityManager;
        this.surveyCreatorManager = surveyCreatorManager;
        this.surveyStatisticsManager = surveyStatisticsManager;
        this.bot = bot;
    }

    public UpdateHandler getHandler(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return new TextUpdateHandler(communityManager, bot);
        } else if (update.hasPollAnswer()) {
            return new PollUpdateHandler(surveyCreatorManager, surveyStatisticsManager);
        }
        return null;
    }
}

