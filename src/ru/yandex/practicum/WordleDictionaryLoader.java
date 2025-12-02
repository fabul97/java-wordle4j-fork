package ru.yandex.practicum;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary loadDictionary(String filename) throws IOException, DictionaryException {
        log.println("Загрузка словаря из файла: " + filename);

        List<String> fiveLetterWords = new ArrayList<>();
        int totalWords = 0;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                totalWords++;
                String normalized = normalizeLine(line);

                if (normalized.length() == 5 && isValidWord(normalized)) {
                    fiveLetterWords.add(normalized);
                }
            }
        }

        log.println("Всего слов в файле: " + totalWords);
        log.println("Слов из 5 букв: " + fiveLetterWords.size());

        if (fiveLetterWords.isEmpty()) {
            throw new DictionaryException("Словарь не содержит слов из 5 букв");
        }

        return new WordleDictionary(fiveLetterWords, log);
    }

    private String normalizeLine(String line) {
        return line.trim().toLowerCase().replace('ё', 'е');
    }

    private boolean isValidWord(String word) {
        for (char c : word.toCharArray()) {
            if (!isRussianLetter(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRussianLetter(char c) {
        return (c >= 'а' && c <= 'я') || c == 'е';
    }
}

