package com.eleks.academy.whoami.model.response;

import com.eleks.academy.whoami.core.chat.QuestionsHistory;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class GameHistory {

    @Getter
    private final List<QuestionsHistory> questions = new ArrayList<>();
    private QuestionsHistory currentQuestion;

    public void addQuestion(String player, String type, String question) {
        this.currentQuestion = new QuestionsHistory(player, type, question);
        this.questions.add(this.currentQuestion);
    }

    public void addAnswer(String player, String answer) {
        this.questions
                .stream()
                .filter(question -> question.equals(this.currentQuestion))
                .findAny()
                .ifPresent(question -> question.addAnswer(player, answer));
    }
}

