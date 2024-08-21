package org.example;



import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Survey {
    @Getter
    private List<QuestionBlock> questionBlocks;
    private static final int minQuestionCount = 2;
    private static final int maxQuestionCount = 4;
    public Survey(){
        this.questionBlocks = new ArrayList<>();
    }

    public void addQuestionBlock(QuestionBlock questionBlock) {

        questionBlocks.add(questionBlock);
    }

    public boolean hasEnoughAnswers(QuestionBlock questionBlock) {
        return questionBlock.getAnswers().size() >= minQuestionCount && questionBlock.getAnswers().size() <= maxQuestionCount;
    }

    public int getQuestionBlockSize() {
        return questionBlocks.size();
    }






}
