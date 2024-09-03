package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class SurveyStatisticsManager {
    private Map<String, Map<String, Integer>> surveyResponsesMap;
    private Map<Long, Set<String>> userResponsesMap;
    private Map<String, SurveyDetails> pollsMap;
    private CommunityManager communityManager;
    private int statisticsSentCount;
    private static Object lock = new Object();


    public SurveyStatisticsManager(Map<String, SurveyDetails> pollsMap, CommunityManager communityManager) {
        this.surveyResponsesMap=new HashMap<>();
        this.userResponsesMap=new HashMap<>();
        this.pollsMap = pollsMap;
        this.communityManager = communityManager;
        this.statisticsSentCount=0;
        printAllMaps();
        checkIfEnoughTimePassedToGatherStatistics();
    }
    private synchronized void  checkIfEnoughTimePassedToGatherStatistics() {
        new Thread(()->{
            while (true) {
                synchronized (lock) {
                    LocalDateTime localDateTime=LocalDateTime.now();
                    if(this.communityManager.getSurveyCreatorManager().checkIfEnoughTimePassed(localDateTime)) {
                        sendStatisticsToCreator();
                        resetStatistics();
                        ActiveSurveyManager.getInstance().endSurvey();
                        this.communityManager.getSurveyCreatorManager().setTimePollSentOutNull();
                    }
                }
            }
        }).start();
    }

        private synchronized void  printAllMaps(){
        new Thread(()->{
            while (true){
                synchronized (lock) {
                    System.out.println("surveyResponsesMap: " + this.surveyResponsesMap);
                    System.out.println("userResponsesMap: " + this.userResponsesMap);
                    System.out.println("pollsMap: " + this.pollsMap);
                    System.out.println("#####################################################################");
                }try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void recordAnswer(String question, String option, Long userId) {
        this.surveyResponsesMap.putIfAbsent(question, new HashMap<>());
        this.userResponsesMap.putIfAbsent(userId, new HashSet<>());
        // שמירת התשובות
        Map<String, Integer> responses = this.surveyResponsesMap.get(question);
        responses.put(option, responses.getOrDefault(option, 0) + 1);
        // שמירת השאלות שעליהן המשתמש ענה
        this.userResponsesMap.get(userId).add(question);
        if (allObserversAnsweredAllQuestions()) {
            sendStatisticsToCreator();
            resetStatistics();
            ActiveSurveyManager.getInstance().endSurvey();
        }
    }

    // בודק אם כל המשתתפים השיבו על כל השאלות
    private boolean allObserversAnsweredAllQuestions() {
        int totalObservers = this.communityManager.getObserversCount() - 1; // כמות המשתתפים ללא יוצר הסקר
        int totalQuestions = 0;
        Set<String> questions = new HashSet<>();
        for (SurveyDetails surveyDetails : this.pollsMap.values()) {
            questions.add(surveyDetails.getQuestion());
        }
        totalQuestions = questions.size();
        for (Set<String> answeredQuestions : this.userResponsesMap.values()) {
            if (answeredQuestions.size() < totalQuestions) {
                return false; // ישנם משתמשים שעדיין לא ענו על כל השאלות
            }
        }
        return this.userResponsesMap.size() >= totalObservers;
    }
    private void sendStatisticsToCreator() {
        Long currentActiveSurveyManagerId=ActiveSurveyManager.getInstance().getActiveUser().getChatId();
        TelegramLongPollingBot currentActiveSurveyManagerBot=ActiveSurveyManager.getInstance().getActiveUser().getBot();
        Set<String> processedQuestions = new HashSet<>(); // סט לשמירת השאלות שכבר עיבדנו
        // לולאה על כל השאלות במפת הסקרים
        for (String pollId : this.pollsMap.keySet()) {
            SurveyDetails surveyDetails = this.pollsMap.get(pollId);
            String question = surveyDetails.getQuestion();
            Map<String, Integer> responses = this.surveyResponsesMap.get(question);
            if (responses == null || responses.isEmpty()) {
                Utils.sendMessageToUser(currentActiveSurveyManagerId
                        ,"אף אחד לא ענה על השאלה: " + question
                            ,currentActiveSurveyManagerBot);
                                continue; // מדלג על השאלה אם אין תגובות שנרשמו
            }
            int totalResponses = totalResponses(responses);
            String statisticsMessage=statisticsMessageBuilder(surveyDetails,responses,totalResponses);
            try {
                Utils.sendMessageToUser(currentActiveSurveyManagerId, statisticsMessage, currentActiveSurveyManagerBot);
                this.statisticsSentCount++;
                processedQuestions.add(question);// הוספת השאלה לסט כדי למנוע עיבוד חוזר
            } catch (Exception e) {
                System.out.println("Error occurred while sending message:");
                e.printStackTrace();
            }
        }
    }
    private int totalResponses(Map<String, Integer> responses){
        return responses.values().stream().mapToInt(Integer::intValue).sum();
    }
    private String statisticsMessageBuilder(SurveyDetails surveyDetails,Map<String, Integer> responses,int totalResponses){
        StringBuilder statisticsMessage = new StringBuilder("תוצאות עבור שאלה: " + surveyDetails.getQuestion() +
                "\n"+"סך תשובות שניקלטו:"+totalResponses+"\n");
        for (String option : surveyDetails.getOptions()) {
            int count = responses.getOrDefault(option, 0);
            double percentage = (count / (double) totalResponses) * 100;
            statisticsMessage.append(String.format("%s: %.2f%%\n", option, percentage));
        }
        return statisticsMessage.toString();
    }
    private void resetStatistics() {
        this.surveyResponsesMap.clear();
        this.userResponsesMap.clear();
        this.pollsMap.clear();
        this.statisticsSentCount = 0;
    }
}