package com.testing.services;

import com.opencsv.exceptions.CsvException;
import com.testing.config.AppConfig;
import com.testing.dao.DictDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class DictService {
    private final DictDao dictDao;
    private final AppConfig.GameProperties props;

    @Autowired
    public DictService(DictDao dictDao, AppConfig.GameProperties props) {
        this.dictDao = dictDao;
        this.props = props;
    }

    public void startTest() {
        Map<String, String> dict;
        try {
            dict = dictDao.collectDict(props.count(), props.from());
        } catch (IOException | CsvException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        final List<Integer> scores = new ArrayList<>();
        int lastScore = 0;
        Scanner scanner = new Scanner(System.in);

        while (true) {
            List<String> keys = new ArrayList<>(dict.keySet());
            Collections.shuffle(keys);
            for (String key : keys) {
                System.out.printf("%s - ", dict.get(key));
                if (scanner.nextLine().equals(key)) {
                    System.out.println("You cool!\n");
                    lastScore += 1;
                } else {
                    System.out.printf("%s - %s\n", key, dict.get(key));
                    scanner.nextLine();
                }
            }
            System.out.printf("Your score: %d.\nPress Enter to restart.", lastScore);
            scanner.nextLine();

            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (IOException | InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
            System.out.printf("Your score: %d.\n", lastScore);
            if (!scores.isEmpty()) {
                System.out.printf("Previous results: %s.\n\n", scores);
            } else {
                System.out.println();
            }
            scores.add(lastScore);
            lastScore = 0;
        }

    }
}
