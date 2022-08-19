import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class Markov {
    private Pattern endOfSentence = Pattern.compile("[\\.\\?\\!\\;]|(\\-\\-)");

    private HashMap<String, LinkedList<BigramFrequency>> nextWordBigramFrequencies;

    public Markov() {
        this.nextWordBigramFrequencies = new HashMap<>();
    }

    public void train(ArrayList<String[]> sentences) {
        for (String[] sentence : sentences) {
            trainSentence(sentence);
        }
    }

    private void trainSentence(String[] sentence) {
        for (int i = 0; i < sentence.length - 2; i++) {
            String firstWord = sentence[i];
            String secondWord = sentence[i + 1];
            String thirdWord = sentence[i + 2];

            if (!nextWordBigramFrequencies.containsKey(firstWord)) {
                nextWordBigramFrequencies.put(firstWord, new LinkedList<>());
            }
            LinkedList<BigramFrequency> bigramFrequencies = this.nextWordBigramFrequencies.get(firstWord);

            boolean foundSecondWord = false;
            for (BigramFrequency bigramFrequency : bigramFrequencies) {
                if (bigramFrequency.getWord().equals(secondWord)) {
                    foundSecondWord = true;
                    boolean foundThirdWord = false;
                    LinkedList<WordFrequency> wordFrequencies = bigramFrequency.getWordFrequencies();
                    for (WordFrequency wordFrequency : wordFrequencies) {
                        if (wordFrequency.getWord().equals(thirdWord)) {
                            wordFrequency.tally();
                            foundThirdWord = true;
                            break;
                        }
                    }
                    if (!foundThirdWord) {
                        wordFrequencies.add(new WordFrequency(bigramFrequency, thirdWord, 1));
                    }
                    break;
                }
            }

            if (!foundSecondWord) {
                BigramFrequency bigramFrequency = new BigramFrequency(secondWord);
                bigramFrequency.getWordFrequencies().add(new WordFrequency(bigramFrequency, thirdWord, 1));
                bigramFrequencies.add(bigramFrequency);
            }
        }
    }

    public String generateSentence() {
        StringBuilder builder = new StringBuilder();

        // Random choices for first two words
        Object[] possibleFirstWords = this.nextWordBigramFrequencies.keySet().toArray();
        int indexOfFirstWord = (int) (Math.random() * possibleFirstWords.length);
        String firstWord = (String) possibleFirstWords[indexOfFirstWord];
        LinkedList<BigramFrequency> bigramFrequencies = this.nextWordBigramFrequencies.get(firstWord);

        int indexOfSecondWord = (int) (Math.random() * bigramFrequencies.size());

        BigramFrequency bigramFrequency = bigramFrequencies.get(indexOfSecondWord);
        String secondWord = bigramFrequency.getWord();

        // Append first two words to build
        builder.append(firstWord);
        builder.append(" ");
        builder.append(secondWord);
        builder.append(" ");

        String thirdWord = null;

        // Generate new words until end of sentence detected
        boolean hasFinishedSentence = false;
        while (!hasFinishedSentence) {
            bigramFrequencies = this.nextWordBigramFrequencies.get(firstWord);

            for (BigramFrequency possibleBigramFrequency : bigramFrequencies) {
                if (possibleBigramFrequency.getWord().equals(secondWord)) {
                    bigramFrequency = possibleBigramFrequency;
                    break;
                }
            }

            int diceRoll = (int) (Math.random() * bigramFrequency.totalFrequency);
            int frequencySoFar = 0;
            for (WordFrequency wordFrequency : bigramFrequency.getWordFrequencies()) {
                frequencySoFar += wordFrequency.frequency;

                if (diceRoll < frequencySoFar) {
                    thirdWord = wordFrequency.getWord();
                    builder.append(thirdWord);
                    builder.append(" ");

                    if (thirdWord.matches(endOfSentence.pattern())) {
                        hasFinishedSentence = true;
                    } else {
                        firstWord = secondWord;
                        secondWord = thirdWord;
                    }

                    break;
                }
            }
        }

        return builder.toString();
    }

    private class BigramFrequency {
        private String word;
        private LinkedList<WordFrequency> wordFrequencies;
        private int totalFrequency;

        public BigramFrequency(String word) {
            this.word = word;
            this.wordFrequencies = new LinkedList<>();
            this.totalFrequency = 0;
        }

        public String getWord() {
            return word;
        }

        public LinkedList<WordFrequency> getWordFrequencies() {
            return wordFrequencies;
        }

        public void tallyTotal() {
            this.totalFrequency++;
        }
    }

    private class WordFrequency {
        private final BigramFrequency bigramFrequency;

        private final String word;

        private int frequency;

        public WordFrequency(BigramFrequency bigramFrequency, String word, int frequency) {
            this.bigramFrequency = bigramFrequency;
            this.word = word;
            this.frequency = frequency;
        }

        public String getWord() {
            return word;
        }

        public void tally() {
            this.frequency++;
            this.bigramFrequency.tallyTotal();
        }
    }
}
