package ru.yandex.practicum;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */

public class Wordle {

    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle_log.txt";

    public static void main(String[] args) {
        PrintWriter log = null;

        try {
            log = new PrintWriter(new FileWriter(LOG_FILE));
            log.println("=== Запуск игры Wordle ===");
            log.println("Дата и время: " + java.time.LocalDateTime.now());

            WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
            WordleDictionary dictionary = loader.loadDictionary(DICTIONARY_FILE);

            WordleGame game = new WordleGame(dictionary, log);

            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║        Добро пожаловать в Wordle!      ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.println();
            System.out.println("Правила игры:");
            System.out.println("• Отгадайте слово из 5 букв за 6 попыток");
            System.out.println("• После каждой попытки вы получите подсказку:");
            System.out.println("  + буква на правильном месте");
            System.out.println("  ^ буква есть в слове, но не на этом месте");
            System.out.println("  - буквы нет в слове");
            System.out.println("• Нажмите Enter без ввода для получения подсказки");
            System.out.println();

            runGameLoop(game, log);

            System.out.println();
            if (game.isWon()) {
                System.out.println("╔════════════════════════════════════════╗");
                System.out.println("║           Поздравляем!                 ║");
                System.out.println("║       Вы отгадали слово!               ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println("Попыток использовано: " + game.getAttemptsCount());
            } else {
                System.out.println("╔════════════════════════════════════════╗");
                System.out.println("║            Игра окончена               ║");
                System.out.println("║      Попытки закончились :(            ║");
                System.out.println("╚════════════════════════════════════════╝");
                System.out.println("Правильное слово было: " + game.getAnswer().toUpperCase());
            }

            log.println("=== Игра завершена ===");

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
            if (log != null) {
                log.println("ОШИБКА IO: " + e.getMessage());
                e.printStackTrace(log);
            } else {
                e.printStackTrace();
            }
        } catch (DictionaryException e) {
            System.err.println("Ошибка словаря: " + e.getMessage());
            if (log != null) {
                log.println("ОШИБКА: " + e.getMessage());
                e.printStackTrace(log);
            }
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка: " + e.getMessage());
            if (log != null) {
                log.println("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
                e.printStackTrace(log);
            } else {
                e.printStackTrace();
            }
        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

    private static void runGameLoop(WordleGame game, PrintWriter log) {
        Scanner scanner = new Scanner(System.in);

        while (!game.isFinished()) {
            System.out.println("─────────────────────────────────────────");
            System.out.println("Осталось попыток: " + game.getStepsLeft());
            System.out.print("Введите слово (или Enter для подсказки): ");

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                String hint = game.getHint();
                if (hint != null) {
                    System.out.println("ПОДСКАЗКА: " + hint.toUpperCase());
                } else {
                    System.out.println("ПРЕДУПРЕЖДЕНИЕ: Подходящих слов не найдено!");
                }
                continue;
            }

            try {
                String result = game.makeGuess(input);
                System.out.println("> " + input.toLowerCase());
                System.out.println("> " + result);

                if (game.isWon()) {
                    break;
                }

            } catch (WordNotFoundInDictionary e) {
                System.out.println("ОШИБКА: " + e.getMessage());
                System.out.println("Попытка не засчитана. Попробуйте другое слово.");
                log.println("Игровое исключение: " + e.getMessage());

            } catch (InvalidInputException e) {
                System.out.println("ОШИБКА: " + e.getMessage());
                System.out.println("Попытка не засчитана. Попробуйте ещё раз.");
                log.println("Игровое исключение: " + e.getMessage());

            } catch (Exception e) {
                System.out.println("ОШИБКА: Произошла ошибка при обработке попытки");
                log.println("ОШИБКА при обработке попытки:");
                e.printStackTrace(log);
            }
        }

        scanner.close();
    }
}