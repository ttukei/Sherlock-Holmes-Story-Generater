import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {
    public static void main(String[] args) {
        String houn = cleanDoc(loadDoc("Resources/houn.txt"));
        String sign = cleanDoc(loadDoc("Resources/sign.txt"));
        String stud = cleanDoc(loadDoc("Resources/stud.txt"));
        String vall = cleanDoc(loadDoc("Resources/vall.txt"));

        Markov markov = new Markov();

        markov.train(parseSentences(houn));
        markov.train(parseSentences(sign));
//        markov.train(parseSentences(stud));
//        markov.train(parseSentences(vall));

        try (FileWriter fileWriter = new FileWriter("README.txt")) {
            for (int sentenceCount = 0; sentenceCount < 150; sentenceCount++) {
                String sentence = markov.generateSentence();
                fileWriter.append(sentence).append("\n");
                System.out.println("Sentence " + sentenceCount);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static String loadDoc(String filename) {
        try {
            File file = new File(filename);
            Scanner sc = new Scanner(file);
            StringBuilder text = new StringBuilder();
            while (sc.hasNext())
            {
                text.append(sc.nextLine());
            }
            return text.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String cleanDoc(String document) {
        return document
                .replaceAll("[\\\"\\\'\\,\\:]", "")
                .toLowerCase();
    }

    public static ArrayList<String[]> parseSentences(String text) {
        Pattern sentenceSplitter = Pattern.compile("[\\.\\?\\!\\;]|(\\-\\-)");
        Pattern wordSplitter = Pattern.compile("(\\s|[\\.\\?\\!\\;\\,]|(\\-\\-))+");

        ArrayList<String> sentences = new ArrayList<>();

        Matcher matcher = sentenceSplitter.matcher(text);

        int sentenceStart = 0;
        int textLength = text.length();
        while (sentenceStart < textLength && matcher.find(sentenceStart)) {
            int sentenceEnd = matcher.end();
            String sentence = text.substring(sentenceStart, sentenceEnd);
            sentences.add(sentence);
            sentenceStart = sentenceEnd + 1;
        }
        // Add remainder as last sentence (if any)
        if (sentenceStart < textLength) {
            sentences.add(text.substring(sentenceStart));
        }

        // Parse sentences into words
        ArrayList<String[]> parsedSentences = new ArrayList<>();
        for (String sentence : sentences) {
            matcher = wordSplitter.matcher(sentence);
            int wordStart = 0;

            ArrayList<String> words = new ArrayList<>();

            while (wordStart < sentence.length() && matcher.find(wordStart)) {
                int wordEnd = matcher.start();

                String word;
                word = sentence.substring(wordStart, wordEnd).trim();

                words.add(word);
                wordStart = matcher.end();

                // Add ending punctuation
                if (matcher.end() == sentence.length()) {
                    words.add(sentence.substring(matcher.start(), matcher.end()));
                }
            }

            if (wordStart < sentence.length()) {
                words.add(sentence.substring(wordStart));
            }

            String[] parsedSentence = new String[words.size()];
            for (int i = 0; i < words.size(); i++) {
                parsedSentence[i] = words.get(i);
            }
            parsedSentences.add(parsedSentence);
        }

        return parsedSentences;
    }
}
