package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private final List<String> words;
    private final PrintWriter log;

    public WordleDictionary(List<String> words, PrintWriter log) {
        this.words = new ArrayList<>(words);
        this.log = log;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new RuntimeException("Словарь пуст, невозможно выбрать случайное слово");
        }
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    public boolean contains(String word) {
        return words.contains(normalize(word));
    }

    public int size() {
        return words.size();
    }

    public static String normalize(String word) {
        return word.toLowerCase().replace('ё', 'е');
    }

    public static String compareWords(String guess, String answer) {
        if (guess.length() != answer.length()) {
            throw new IllegalArgumentException("Слова должны быть одинаковой длины");
        }

        StringBuilder result = new StringBuilder();
        char[] guessChars = guess.toCharArray();
        char[] answerChars = answer.toCharArray();
        boolean[] usedInAnswer = new boolean[answer.length()];

        for (int i = 0; i < guessChars.length; i++) {
            if (guessChars[i] == answerChars[i]) {
                result.append('+');
                usedInAnswer[i] = true;
            } else {
                result.append('?'); // временная метка
            }
        }

        for (int i = 0; i < guessChars.length; i++) {
            if (result.charAt(i) == '?') {
                boolean found = false;
                for (int j = 0; j < answerChars.length; j++) {
                    if (!usedInAnswer[j] && guessChars[i] == answerChars[j]) {
                        result.setCharAt(i, '^');
                        usedInAnswer[j] = true;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    result.setCharAt(i, '-');
                }
            }
        }

        return result.toString();
    }

    public List<String> filterByAttempts(List<WordAttempt> attempts) {
        if (attempts.isEmpty()) {
            return new ArrayList<>(words);
        }

        List<String> filtered = new ArrayList<>(words);

        for (WordAttempt attempt : attempts) {
            filtered = filterByAttempt(filtered, attempt);
        }

        log.println("После фильтрации осталось слов: " + filtered.size());
        return filtered;
    }

    private List<String> filterByAttempt(List<String> candidates, WordAttempt attempt) {
        List<String> result = new ArrayList<>();

        for (String word : candidates) {
            if (matchesAttempt(word, attempt)) {
                result.add(word);
            }
        }

        return result;
    }

    private boolean matchesAttempt(String word, WordAttempt attempt) {
        String guess = attempt.getWord();
        String hint = attempt.getHint();

        if (word.equals(guess)) {
            return false;
        }

        for (int i = 0; i < guess.length(); i++) {
            char guessChar = guess.charAt(i);
            char hintChar = hint.charAt(i);
            char wordChar = word.charAt(i);

            switch (hintChar) {
                case '+':
                    // Буква должна быть на этом же месте
                    if (wordChar != guessChar) {
                        return false;
                    }
                    break;
                case '^':
                    // Буква есть в слове, но не на этом месте
                    if (wordChar == guessChar) {
                        return false;
                    }
                    if (!word.contains(String.valueOf(guessChar))) {
                        return false;
                    }
                    break;
                case '-':
                    // Буквы не должно быть в слове вообще
                    if (!isCharMarkedElsewhere(guess, hint, guessChar, i)) {
                        if (word.contains(String.valueOf(guessChar))) {
                            return false;
                        }
                    }
                    break;
            }
        }

        return true;
    }

    private boolean isCharMarkedElsewhere(String guess, String hint, char ch, int excludePos) {
        for (int i = 0; i < guess.length(); i++) {
            if (i != excludePos && guess.charAt(i) == ch) {
                char h = hint.charAt(i);
                if (h == '+' || h == '^') {
                    return true;
                }
            }
        }
        return false;
    }

    public static class WordAttempt {
        private final String word;
        private final String hint;

        public WordAttempt(String word, String hint) {
            this.word = word;
            this.hint = hint;
        }

        public String getWord() {
            return word;
        }

        public String getHint() {
            return hint;
        }
    }
}

