package ru.yandex.practicum;

public class WordNotFoundInDictionary extends Exception {
    public WordNotFoundInDictionary(String word) {
        super("Слово '" + word + "' не найдено в словаре");
    }
}
