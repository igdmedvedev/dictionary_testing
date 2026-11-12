package com.testing.dao;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.testing.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class DictDao {
    private final String pathFile;

    @Autowired
    public DictDao(AppConfig.GameProperties props) {
        this.pathFile = props.path();
    }

    public Map<String, String> collectDict(@Nullable Integer countOfWords, int fromWord) throws IOException, CsvException {
        final CSVParser parser = new CSVParserBuilder()
                .withSeparator('#')
                .build();

        final CSVReader csvReader = new CSVReaderBuilder(new FileReader(pathFile))
                .withCSVParser(parser)
                .withSkipLines(fromWord - 1) //отсекли лишние строки из начала файла
                .build();

        try (csvReader) {
            List<String[]> lines = csvReader.readAll();

            if (countOfWords == null) { //все строки до конца файла
                countOfWords = lines.size();
            } else { //заданное число не должно быть больше чем строк в файле, но это не должно быть ошибкой - выводим все строки до последней
                countOfWords = Integer.min(countOfWords, lines.size());
            }

            Map<String, String> dict = new HashMap<>();
            for (String[] arr : lines.subList(0, countOfWords)) { //отсекаем лишние строки из конца файла
                dict.put(arr[0], arr[1]);
            }
            return dict;
        }
    }
}
