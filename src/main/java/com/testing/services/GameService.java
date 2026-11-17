package com.testing.services;

import com.opencsv.exceptions.CsvException;
import com.testing.config.AppConfig;
import com.testing.dao.DictDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class GameService {
    public record AnswerResult(boolean correct, String word, String definition) {
    }

    private final DictDao dictDao;
    private final AppConfig.GameProperties props;

    private Map<String, String> dictionary = Map.of();
    private Deque<String> currentRound = new ArrayDeque<>();
    private int currentScore;
    private final List<Integer> finishedRounds = new ArrayList<>();

    @Autowired
    public GameService(DictDao dictDao, AppConfig.GameProperties props) {
        this.dictDao = dictDao;
        this.props = props;
    }

    public void start() throws IOException, CsvException {
        dictionary = dictDao.collectDict(props.count(), props.from());
        if (dictionary.isEmpty()) {
            throw new IllegalStateException("Dictionary is empty: " + props.path());
        }
        currentScore = 0;
        finishedRounds.clear();
        startRound();
    }

    public boolean hasNextQuestion() {
        return !currentRound.isEmpty();
    }

    public String currentDefinition() {
        return dictionary.get(currentWord());
    }

    public AnswerResult submit(String guess) {
        String word = currentRound.poll();
        if (word == null) {
            throw new IllegalStateException("No active question: round is over");
        }
        boolean correct = word.equals(guess);
        if (correct) {
            currentScore++;
        }
        return new AnswerResult(correct, word, dictionary.get(word));
    }

    public int currentScore() {
        return currentScore;
    }

    public List<Integer> finishedRounds() {
        return List.copyOf(finishedRounds);
    }

    public void finishRound() {
        finishedRounds.add(currentScore);
        currentScore = 0;
        startRound();
    }

    private void startRound() {
        List<String> words = new ArrayList<>(dictionary.keySet());
        Collections.shuffle(words);
        currentRound = new ArrayDeque<>(words);
    }

    private String currentWord() {
        String word = currentRound.peek();
        if (word == null) {
            throw new IllegalStateException("No active question: round is over");
        }
        return word;
    }
}
