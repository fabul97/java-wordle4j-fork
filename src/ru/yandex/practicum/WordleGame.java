package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */

public class WordleGame {

    private static final int MAX_STEPS = 6;
    private static final int WORD_LENGTH = 5;

    private final String answer;
    private int steps;
    private final WordleDictionary dictionary;
    private final PrintWriter log;
    private final List<WordleDictionary.WordAttempt> attempts;
    private boolean isWon;
    private boolean isFinished;
    private final Set<String> usedHints;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this.dictionary = dictionary;
        this.log = log;
        this.answer = dictionary.getRandomWord();
        this.steps = MAX_STEPS;
        this.attempts = new ArrayList<>();
        this.usedHints = new HashSet<>();  // ← ДОБАВИТЬ
        this.isWon = false;
        this.isFinished = false;

        log.println("Игра создана. Загаданное слово: " + answer);
        log.println("Количество шагов: " + MAX_STEPS);
    }

    public String makeGuess(String input) throws WordNotFoundInDictionary, InvalidInputException {
        if (isFinished) {
            throw new RuntimeException("Игра уже завершена");
        }

        // Нормализуем ввод
        String normalizedInput = WordleDictionary.normalize(input.trim());

        // Проверяем длину
        if (normalizedInput.length() != WORD_LENGTH) {
            throw new InvalidInputException("Слово должно содержать ровно " + WORD_LENGTH + " букв");
        }

        // Проверяем на корректность символов
        if (!isValidRussianWord(normalizedInput)) {
            throw new InvalidInputException("Слово должно содержать только русские буквы");
        }

        // Проверяем наличие в словаре
        if (!dictionary.contains(normalizedInput)) {
            throw new WordNotFoundInDictionary(normalizedInput);
        }

        // Уменьшаем количество попыток
        steps--;
        log.println("Попытка #" + (MAX_STEPS - steps) + ": " + normalizedInput);

        // Проверяем совпадение с ответом
        if (normalizedInput.equals(answer)) {
            isWon = true;
            isFinished = true;
            log.println("Игрок выиграл!");
            return "+++++";
        }

        // Сравниваем слова и получаем подсказку
        String hint = WordleDictionary.compareWords(normalizedInput, answer);
        attempts.add(new WordleDictionary.WordAttempt(normalizedInput, hint));

        log.println("Результат: " + hint);
        log.println("Осталось попыток: " + steps);

        // Проверяем, закончились ли попытки
        if (steps == 0) {
            isFinished = true;
            log.println("Игрок проиграл. Попытки закончились.");
        }

        return hint;
    }

    public String getHint() {
        log.println("Запрос подсказки. Количество попыток: " + attempts.size());

        List<String> candidates = dictionary.filterByAttempts(attempts);

        candidates.removeAll(usedHints);

        if (candidates.isEmpty()) {
            log.println("Подходящих слов не найдено!");
            return null;
        }

        Random random = new Random();
        String hint = candidates.get(random.nextInt(candidates.size()));

        usedHints.add(hint);
        log.println("Подсказка: " + hint + " (всего вариантов: " + candidates.size() + ")");

        return hint;
    }

    private boolean isValidRussianWord(String word) {
        for (char c : word.toCharArray()) {
            if (!((c >= 'а' && c <= 'я') || c == 'е')) {
                return false;
            }
        }
        return true;
    }

    public boolean isWon() {
        return isWon;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public String getAnswer() {
        return answer;
    }

    public int getStepsLeft() {
        return steps;
    }

    public int getAttemptsCount() {
        return attempts.size();
    }
}
