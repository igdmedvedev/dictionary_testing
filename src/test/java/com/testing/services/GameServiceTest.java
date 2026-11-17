package com.testing.services;

import com.testing.config.AppConfig;
import com.testing.dao.DictDao;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Логика игры тестируется без консоли: словарь подменяется заглушкой,
 * терминал нигде не нужен.
 */
class GameServiceTest {

    private static AppConfig.GameProperties props() {
        return new AppConfig.GameProperties("u1.csv", 1, null);
    }

    private static DictDao stubDictDao(Map<String, String> words) {
        return new DictDao(props()) {
            @Override
            public Map<String, String> collectDict(Integer countOfWords, int fromWord) {
                return new HashMap<>(words);
            }
        };
    }

    @Test
    void correctAnswerGivesPoint() throws Exception {
        GameService game = new GameService(stubDictDao(Map.of("apple", "яблоко")), props());
        game.start();

        assertTrue(game.hasNextQuestion());
        assertEquals("яблоко", game.currentDefinition());

        GameService.AnswerResult result = game.submit("apple");
        assertTrue(result.correct());
        assertEquals(1, game.currentScore());
        assertFalse(game.hasNextQuestion());
    }

    @Test
    void wrongAnswerRevealsCorrectWordAndKeepsScore() throws Exception {
        GameService game = new GameService(stubDictDao(Map.of("apple", "яблоко")), props());
        game.start();

        GameService.AnswerResult result = game.submit("banana");
        assertFalse(result.correct());
        assertEquals("apple", result.word());
        assertEquals("яблоко", result.definition());
        assertEquals(0, game.currentScore());
    }

    @Test
    void finishRoundMovesScoreToHistoryAndStartsNewRound() throws Exception {
        GameService game = new GameService(stubDictDao(Map.of("apple", "яблоко")), props());
        game.start();
        game.submit("apple");

        assertEquals(1, game.currentScore());
        assertTrue(game.finishedRounds().isEmpty());

        game.finishRound();

        assertEquals(List.of(1), game.finishedRounds());
        assertEquals(0, game.currentScore());
        assertTrue(game.hasNextQuestion()); // новый раунд уже начался
    }

    @Test
    void historyAccumulatesAcrossRounds() throws Exception {
        GameService game = new GameService(stubDictDao(Map.of("apple", "яблоко")), props());
        game.start();

        game.submit("apple");
        game.finishRound();
        game.submit("wrong");
        game.finishRound();

        assertEquals(List.of(1, 0), game.finishedRounds());
    }

    @Test
    void startWithEmptyDictionaryFails() {
        GameService game = new GameService(stubDictDao(Map.of()), props());
        assertThrows(IllegalStateException.class, game::start);
    }
}
