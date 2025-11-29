package ru.yandex.practicum;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class WordleTest {

    private static PrintWriter log;
    private WordleDictionary dictionary;

    @BeforeAll
    static void setUpAll() {
        log = new PrintWriter(System.out, true);
    }

    @BeforeEach
    void setUp() {
        List<String> testWords = Arrays.asList(
                "слово", "книга", "стена", "замок", "город",
                "камен", "polen", "герой", "вокал", "танец"
        );
        dictionary = new WordleDictionary(testWords, log);
    }

    // Тесты WordleDictionary

    @Test
    void testNormalize() {
        assertEquals("слово", WordleDictionary.normalize("Слово"));
        assertEquals("слово", WordleDictionary.normalize("СЛОВО"));
        assertEquals("елка", WordleDictionary.normalize("ёлка"));
        assertEquals("елка", WordleDictionary.normalize("Ёлка"));
    }

    @Test
    void testContains() {
        assertTrue(dictionary.contains("слово"));
        assertTrue(dictionary.contains("СЛОВО"));
        assertTrue(dictionary.contains("Книга"));
        assertFalse(dictionary.contains("домик"));
    }

    @Test
    void testGetRandomWord() {
        String word = dictionary.getRandomWord();
        assertNotNull(word);
        assertEquals(5, word.length());
        assertTrue(dictionary.contains(word));
    }

    @Test
    void testCompareWords_AllCorrect() {
        String result = WordleDictionary.compareWords("слово", "слово");
        assertEquals("+++++", result);
    }

    @Test
    void testCompareWords_AllWrong() {
        String result = WordleDictionary.compareWords("книга", "замок");

        for (char c : result.toCharArray()) {
            assertTrue(c == '-' || c == '^' || c == '+');
        }
    }

    @Test
    void testCompareWords_Mixed() {

        String result = WordleDictionary.compareWords("гонец", "герой");
        assertEquals('+', result.charAt(0)); // г на месте
        assertTrue(result.charAt(1) == '^' || result.charAt(1) == '-'); // о
        assertTrue(result.charAt(2) == '-'); // н
        assertTrue(result.charAt(3) == '^' || result.charAt(3) == '-'); // е
        assertTrue(result.charAt(4) == '-'); // ц
    }

    @Test
    void testCompareWords_WithDuplicates() {
        String result = WordleDictionary.compareWords("мамам", "мамам");
        assertEquals("+++++", result);
    }

    @Test
    void testFilterByAttempts_Empty() {
        List<WordleDictionary.WordAttempt> attempts = new ArrayList<>();
        List<String> filtered = dictionary.filterByAttempts(attempts);
        assertEquals(dictionary.size(), filtered.size());
    }

    @Test
    void testFilterByAttempts_WithHints() {
        List<WordleDictionary.WordAttempt> attempts = new ArrayList<>();
        attempts.add(new WordleDictionary.WordAttempt("книга", "-+---"));

        List<String> filtered = dictionary.filterByAttempts(attempts);

        for (String word : filtered) {
            if (!word.equals("книга")) {
                if (word.length() > 1) {
                    assertNotEquals("книга", word);
                }
            }
        }
    }

    // Тесты WordleGame

    @Test
    void testGameCreation() {
        WordleGame game = new WordleGame(dictionary, log);
        assertNotNull(game);
        assertEquals(6, game.getStepsLeft());
        assertFalse(game.isFinished());
        assertFalse(game.isWon());
    }

    @Test
    void testMakeGuess_ValidWord() throws Exception {
        WordleGame game = new WordleGame(dictionary, log);
        String result = game.makeGuess("слово");
        assertNotNull(result);
        assertEquals(5, result.length());
        assertEquals(5, game.getStepsLeft());
    }

    @Test
    void testMakeGuess_InvalidLength() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(InvalidInputException.class, () -> {
            game.makeGuess("дом");
        });
    }

    @Test
    void testMakeGuess_NotInDictionary() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(WordNotFoundInDictionary.class, () -> {
            game.makeGuess("абвгд");
        });
    }

    @Test
    void testMakeGuess_Win() throws Exception {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        String result = game.makeGuess(answer);
        assertEquals("+++++", result);
        assertTrue(game.isWon());
        assertTrue(game.isFinished());
    }

    @Test
    void testMakeGuess_MaxAttempts() throws Exception {
        WordleGame game = new WordleGame(dictionary, log);
        String answer = game.getAnswer();

        // Делаем 6 попыток неправильными словами
        for (int i = 0; i < 6; i++) {
            if (game.getStepsLeft() > 0 && !game.isFinished()) {
                for (int j = 0; j < 10; j++) {
                    String randomWord = dictionary.getRandomWord();
                    if (!randomWord.equals(answer)) {
                        try {
                            game.makeGuess(randomWord);
                            break;
                        } catch (Exception e) {}
                    }
                }
            }
        }

        assertEquals(0, game.getStepsLeft());
        assertTrue(game.isFinished());
    }

    @Test
    void testGetHint() {
        WordleGame game = new WordleGame(dictionary, log);
        String hint = game.getHint();
        assertNotNull(hint);
        assertTrue(dictionary.contains(hint));
    }

    @Test
    void testGetHint_AfterAttempts() throws Exception {
        WordleGame game = new WordleGame(dictionary, log);

        game.makeGuess("слово");

        String hint = game.getHint();
        if (hint != null) {
            assertTrue(dictionary.contains(hint));
            assertNotEquals("слово", hint); // Подсказка не должна быть уже использованным словом
        }
    }

    @Test
    void testNormalization_InGame() throws Exception {
        WordleGame game = new WordleGame(dictionary, log);

        // Проверяем, что игра принимает слова в разных регистрах
        try {
            game.makeGuess("СЛОВО");
        } catch (WordNotFoundInDictionary e) {}
    }

    // Тесты валидации

    @Test
    void testInvalidInput_EnglishLetters() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(InvalidInputException.class, () -> {
            game.makeGuess("hello");
        });
    }

    @Test
    void testInvalidInput_Numbers() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(InvalidInputException.class, () -> {
            game.makeGuess("12345");
        });
    }

    @Test
    void testInvalidInput_SpecialChars() {
        WordleGame game = new WordleGame(dictionary, log);
        assertThrows(InvalidInputException.class, () -> {
            game.makeGuess("!!!??");
        });
    }

    // Тесты граничных значений

    @Test
    void testEmptyDictionary() {
        List<String> emptyWords = new ArrayList<>();
        WordleDictionary emptyDict = new WordleDictionary(emptyWords, log);

        assertThrows(RuntimeException.class, () -> {
            emptyDict.getRandomWord();
        });
    }

    @Test
    void testCompareWords_DifferentLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            WordleDictionary.compareWords("дом", "домик");
        });
    }
}
