import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class WordleSolver {

    // Get list of 5 letter words
    private static final List<String> WORD_LIST = getWords();

    // Get map of letters and associated frequency
    private static final Map<String, Integer> LETTER_FREQUENCIES = getLetterFrequency();

    // Get score of each word. This map will get smaller and smaller as words are eliminated.
    // TODO to be improved: first implementation is to simply add up raw frequencies
    private static final Map<String, Integer> WORD_SCORES = getWordScores(LETTER_FREQUENCIES);

    private static final String DICTIONARY_PATH_1 = "/Users/zackthomas/Documents/personal/wordle/src/dictionary/wordle-allowed-guesses.txt";
    private static final String DICTIONARY_PATH_2 = "/Users/zackthomas/Documents/personal/wordle/src/dictionary/wordle-answers-alphabetical.txt";

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

    private static final List<String> BLACKS = new ArrayList<>(Arrays.asList(
            // insert black letters here
            "a", "e", "r", "o", "s", "n", "l", "i", "t", "d", "m", "p", "g", "c", "k", "b"
    ));

    private static final Map<Integer, List<String>> YELLOWS = new HashMap<Integer, List<String>>()
    {
        {
            put(0, new ArrayList<>(Arrays.asList(

            )));
            put(1, new ArrayList<>(Arrays.asList(
                    // insert yellow letters at index 1 here

            )));
            put(2, new ArrayList<>(Arrays.asList(
                    "f"

            )));
            put(3, new ArrayList<>(Arrays.asList(

            )));
            put(4, new ArrayList<>(Arrays.asList(
                    // insert yellow letters at index 4 here

            )));
        }
    };

    private static final Map<Integer, String> GREENS = new HashMap<Integer, String>()
    {
        {
            put(1, "u");
            put(4, "y");

        }
    };

    static List<String> getWords() {
        List<String> wordList = new ArrayList<>();
        try {
            // Load from first dictionary
            File myObj = new File(DICTIONARY_PATH_1);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String word = myReader.nextLine();
                if (word.length() == 5) {
                    wordList.add(word.toLowerCase());
                }
            }
            myReader.close();

            // Load from second dictionary
            myObj = new File(DICTIONARY_PATH_2);
            myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String word = myReader.nextLine();
                wordList.add(word.toLowerCase());
                if (word.length() == 5) {
                    wordList.add(word.toLowerCase());
                }
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return wordList;
    }

    static Map<String, Integer> getLetterFrequency() {
        Map<String, Integer> freqMap = new HashMap<>();
        for (String letter : ALPHABET.split("")) {
            Integer count = 0;
            for (String word : WORD_LIST) {
                for (String character : word.split("")) {
                    if (character.equals(letter)) {
                        count++;
                    }
                }
            }
            freqMap.put(letter, count);
        }
        return freqMap;
    }

    static Map<String, Integer> getWordScores(Map<String, Integer> letterFreqs) {
        Map<String, Integer> wordScores = new HashMap<>();
        for (String word : WORD_LIST) {
            Integer score = 0;
            for (String letter: word.split("")) {
                Integer letterScore = letterFreqs.get(letter);
                score+=letterScore;
            }
            wordScores.put(word, score);
        }
        return wordScores;
    }

    static List<String> getBestWords() {
        Integer maxScore = getMaxScore(WORD_SCORES);
        List<String> bestWords = new ArrayList<>();
        for (String word : WORD_SCORES.keySet()) {
            if (WORD_SCORES.get(word) == maxScore) {
                bestWords.add(word);
            }
        }
        return bestWords;
    }

    static Integer getMaxScore(Map<String, Integer> wordScoresMap) {
        Integer maxScore = 0;
        for (String word : wordScoresMap.keySet()) {
            Integer score = wordScoresMap.get(word);
            // filter out any guess with double letters
            List<String> letters = Arrays.asList(word.split(""));
            Set<String> wordAsSet = new HashSet<>(letters);
            if (score > maxScore && wordAsSet.size()==5) {
                maxScore = score;
            }
        }
        // if nothing was found for no double letters, then we must allow them
        if (maxScore == 0) {
            for (String word : wordScoresMap.keySet()) {
                Integer score = wordScoresMap.get(word);
                if (score > maxScore) {
                    maxScore = score;
                }
            }
        }
        return maxScore;
    }

    static final List<String> getNextGuesses(List<String> blackLetters,
                                             Map<Integer, List<String>> yellowLetterToPositionMap,
                                             Map<Integer, String > greenLetterToPositionMap) {
        // for every letter in blackList, remove any word containing this letter from WORD_LIST
        List<String> wordsToRemove = new ArrayList<>();
        for (String letter : blackLetters) {
            for (String word : WORD_SCORES.keySet()) {
                if (word.contains(letter)) {
                    wordsToRemove.add(word);
                }
            }
        }
        // for every letter in yellowList, remove any word that contains this letter at the given position
        for (Integer index : yellowLetterToPositionMap.keySet()) {
            for (String word : WORD_SCORES.keySet()) {
                List<String> wordAsList = Arrays.asList(word.split(""));
                List<String> badLettersAtIndex = yellowLetterToPositionMap.get(index);
                for (String letter : badLettersAtIndex) {
                    // remove words that contain the yellow letter at the given index
                    if (wordAsList.get(index).equals(letter)) {
                        wordsToRemove.add(word);
                    }
                    // remove words that do not contain the yellow letter at all
                    if (!word.contains(letter)) {
                        wordsToRemove.add(word);
                    }
                }

            }
        }
        // for every letter in greenList, remove any word that does NOT contain this letter at the given position
        for (Integer index : greenLetterToPositionMap.keySet()) {
            for (String word : WORD_SCORES.keySet()) {
                List<String> wordAsList = Arrays.asList(word.split(""));
                if (!wordAsList.get(index).equals(greenLetterToPositionMap.get(index))) {
                    wordsToRemove.add(word);
                }
            }
        }
        for (String word : wordsToRemove) {
            WORD_SCORES.remove(word);
        }
        return getBestWords();
    }

    public static void main(String[] args) {
        // Word with highest score is 'aeros'. This will be the first guess.
        // TODO during the game: update the BLACK, YELLOW, and GREEN lists
        System.out.print(getNextGuesses(BLACKS, YELLOWS, GREENS));
    }
}