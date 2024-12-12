package main.java.com.dinododge.game;

import com.badlogic.gdx.utils.Array;

public class Question {
    public String questionText;
    public Array<String> options;
    public int correctOptionIndex;

    public Question(String questionText, Array<String> options, int correctOptionIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }
}