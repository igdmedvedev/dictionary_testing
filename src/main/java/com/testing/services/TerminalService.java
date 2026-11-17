package com.testing.services;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Всё взаимодействие с консолью: вывод сообщений, чтение ввода, очистка экрана.
 *
 * <p>Также ведёт бесконечную интерактивную сессию игры: опрашивает
 * {@link GameService} и показывает результаты. Сама логика игры (раунды,
 * счёт, подбор слов) в этот класс не входит — он только показывает и
 * считывает, поэтому {@link GameService} от него не зависит.</p>
 */
@Component
public class TerminalService {

    private final GameService game;
    private final PrintStream out = System.out;
    private final Scanner in = new Scanner(System.in);

    @Autowired
    public TerminalService(GameService game) {
        this.game = game;
    }

    /**
     * Запускает бесконечную игру: раунд за раундом, пока процесс не завершён.
     */
    public void startTest() {
        try {
            game.start();
        } catch (IOException | CsvException ex) {
            println(ex.getMessage());
            return;
        }

        while (true) {
            while (game.hasNextQuestion()) {
                printf("%s - ", game.currentDefinition());
                GameService.AnswerResult result = game.submit(readLine());
                if (result.correct()) {
                    println("You cool!\n");
                } else {
                    printf("%s - %s\n", result.word(), result.definition());
                    readLine(); // пауза, чтобы успеть прочитать верный ответ
                }
            }

            printf("Your score: %d.\nPress Enter to restart.", game.currentScore());
            readLine();
            clearScreen();

            printf("Your score: %d.\n", game.currentScore());
            if (game.finishedRounds().isEmpty()) {
                println();
            } else {
                printf("Previous results: %s.\n\n", game.finishedRounds());
            }
            game.finishRound();
        }
    }

    // ------------------------------------------------------------------
    // Базовые операции с терминалом
    // ------------------------------------------------------------------

    public void print(String text) {
        out.print(text);
    }

    public void println() {
        out.println();
    }

    public void println(String text) {
        out.println(text);
    }

    public void printf(String format, Object... args) {
        out.printf(format, args);
    }

    /** Читает одну строку ввода. */
    public String readLine() {
        return in.nextLine();
    }

    /**
     * Очищает экран кросс-платформенно: "cls" на Windows, "clear" на
     * Linux/macOS. Если соответствующая команда недоступна, использует
     * ANSI-последовательность (поддерживается терминалами Linux/macOS
     * и Windows 10+).
     */
    public void clearScreen() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        ProcessBuilder builder = osName.contains("win")
                ? new ProcessBuilder("cmd", "/c", "cls")
                : new ProcessBuilder("clear");
        try {
            builder.inheritIO();
            builder.start().waitFor();
        } catch (IOException ex) {
            printf("\u001B[2J\u001B[3J\u001B[H");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            printf("\u001B[2J\u001B[3J\u001B[H");
        }
    }
}
