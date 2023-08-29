package bg.sofia.uni.fmi.mjt.sentiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MovieReviewSentimentAnalyzer implements SentimentAnalyzer {

    private static final String WORDS_SEPARATOR = "[^\\w']";
    private static final char APOSTROPHE = '\'';
    private static final int MIN_WORD_LENGTH = 2;
    private static final String SPACE = " ";
    private static Set<String> stopWords;  // Stopwords will be the same in every object, make them static to save memory
    private final Writer reviewsOut;
    private final Map<String, Double> sentimentScore;
    private final Map<String, Integer> countWords;
    private List<Content> reviews;

    public MovieReviewSentimentAnalyzer(Reader stopWordsIn, Reader reviewsIn, Writer reviewsOut) {
        readStopWords(stopWordsIn);
        readReviews(reviewsIn);
        this.reviewsOut = reviewsOut;
        sentimentScore = new HashMap<>();
        countWords = new HashMap<>();
        calculateSentimentScoreAllWords();
    }

    @Override
    public double getReviewSentiment(String review) {
        String[] words = review.strip().toLowerCase().split(WORDS_SEPARATOR);
        double sum = 0;
        int counter = 0;
        int flag = 0;

        for (String currWord : words) {
            if (sentimentScore.containsKey(currWord.toLowerCase())) {
                sum += sentimentScore.get(currWord.toLowerCase());
                counter++;
                flag = 1;
            }
        }

        if (flag == 0) {
            return Rating.unknown.getRating();
        }

        if (counter == 0) {
            throw new IllegalStateException("Error: dividing by zero");
        }

        return sum / counter;
    }

    @Override
    public String getReviewSentimentAsName(String review) {
        double sentimentScore = getReviewSentiment(review.toLowerCase());

        if (sentimentScore == Rating.unknown.getRating()) {
            return Rating.unknown.name();
        }

        long roundedSentimentScore = Math.round(sentimentScore);

        if (roundedSentimentScore == Rating.negative.getRating()) {
            return Rating.negative.name();
        } else if (roundedSentimentScore == Rating.somewhatNegative.getRating()) {
            return Rating.somewhatNegative.name();
        } else if (roundedSentimentScore == Rating.neutral.getRating()) {
            return Rating.neutral.name();
        } else if (roundedSentimentScore == Rating.somewhatPositive.getRating()) {
            return Rating.somewhatPositive.name();
        } else if (roundedSentimentScore == Rating.positive.getRating()) {
            return Rating.positive.name();
        }

        return null;
    }

    @Override
    public double getWordSentiment(String word) {
        if (sentimentScore.containsKey(word.toLowerCase())) {
            return sentimentScore.get(word.toLowerCase());
        }

        return Rating.unknown.getRating();
    }

    @Override
    public int getWordFrequency(String word) {
        if (!countWords.containsKey(word.toLowerCase()) || stopWords.contains(word.toLowerCase())) {
            return 0;
        }

        return countWords.get(word.toLowerCase());
    }

    @Override
    public List<String> getMostFrequentWords(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The limit can not be a negative number");
        }

        Set<String> keys = countWords.keySet();

        return keys.stream()
                .sorted((e1, e2) -> countWords.get(e2).compareTo(countWords.get(e1)))
                .limit(n)
                .toList();
    }

    @Override
    public List<String> getMostPositiveWords(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The limit can not be negative number");
        }

        var sortedEntry = sentimentScore.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .toList();

        return  findFirstNElementsInList(sortedEntry, n);
    }

    @Override
    public List<String> getMostNegativeWords(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The limit can not be negative number");
        }

        var sortedEntry = sentimentScore.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();

        return  findFirstNElementsInList(sortedEntry, n);
    }

    @Override
    public boolean appendReview(String review, int sentiment) {
        if (review == null || review.isEmpty() || review.isBlank()) {
            throw new IllegalArgumentException("Review text can not be null, empty or blank");
        }

        if (sentiment < Rating.negative.getRating() || sentiment > Rating.positive.getRating()) {
            throw new IllegalArgumentException("Sentiment's value must be between 0 and 4");
        }

        try {
            var bufferedWriter = new BufferedWriter(reviewsOut);
            String sentimentString = String.valueOf(sentiment);

            bufferedWriter.write(sentimentString);
            bufferedWriter.write(SPACE);
            bufferedWriter.write(review);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            String line = sentimentString + SPACE + review;
            Content newContent = Content.of(line.toLowerCase());
            reviews.add(newContent);
            updateAfterAppending(review.toLowerCase());

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getSentimentDictionarySize() {
        return sentimentScore.size();
    }

    @Override
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    private void readStopWords(Reader stopWordsIn) {
        var bufferedReader = new BufferedReader(stopWordsIn);
        stopWords = bufferedReader.lines()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    private void readReviews(Reader reviewsIn) {
        var bufferedReader = new BufferedReader(reviewsIn);
        reviews = bufferedReader.lines()
                .map(Content::of)
                .collect(Collectors.toList());
    }

    private boolean isValidWord(String word) {
        int counter = 0;

        if (word.length() < MIN_WORD_LENGTH) {
            return false;
        }

        for (int i = 0; i < word.length(); i++) {
            if (Character.isLetterOrDigit(word.charAt(i)) || word.charAt(i) == APOSTROPHE) {
                counter++;
            }
        }

        return counter >= MIN_WORD_LENGTH;
    }

    private double calculateSumRatingForWord(String word, List<Content> filteredReviews) {
        double sumRating = 0;

        for (Content currContent : filteredReviews) {
            String[] words = currContent.review().strip().toLowerCase().split(WORDS_SEPARATOR);
            for (String currWord : words) {
                if (currWord.equals(word.toLowerCase())) {
                    sumRating += currContent.rating().getRating();
                    break;
                }
            }
        }

        return sumRating;
    }

    private int calculateCountForSentimentScore(String word, List<Content> filteredReviews) {
        int count = 0;

        for (Content currContent : filteredReviews) {
            String[] words = currContent.review().strip().toLowerCase().split(WORDS_SEPARATOR);
            for (String currWord : words) {
                if (currWord.equals(word.toLowerCase())) {
                    count++;
                    break;
                }
            }
        }

        return count;
    }

    private int countAllAppearancesOfWord(String word, List<Content> filteredReviews) {
        int count = 0;

        for (Content currContent : filteredReviews) {
            String[] words = currContent.review().strip().toLowerCase().split(WORDS_SEPARATOR);
            for (String currWord : words) {
                if (currWord.equals(word.toLowerCase())) {
                    count++;
                }
            }
        }

        return count;
    }

    private double calculateSentimentScoreForWord(String word) {
        List<Content> filteredReviews = reviews.stream()
                .filter(e -> e.review().contains(word.toLowerCase()))
                .toList();

        double sumRating = calculateSumRatingForWord(word.toLowerCase(), filteredReviews);
        int count = calculateCountForSentimentScore(word.toLowerCase(), filteredReviews);

        if (count == 0) {
            throw new IllegalStateException("Error: dividing by zero");
        }

        return sumRating / count;
    }

    private void updateWordCount(String word) {
        List<Content> filteredReviews = reviews.stream()
                .filter(e -> e.review().contains(word.toLowerCase()))
                .toList();

        int allAppearances = countAllAppearancesOfWord(word.toLowerCase(), filteredReviews);
        countWords.put(word.toLowerCase(), allAppearances);
    }

    private boolean acceptSentimentWord(String currWord) {
        return !sentimentScore.containsKey(currWord.toLowerCase()) && !stopWords.contains(currWord.toLowerCase())
                && isValidWord(currWord);
    }

    private void calculateSentimentScoreAllWords() {
        for (Content currReview : reviews) {
            String[] words = currReview.review().strip().toLowerCase().split(WORDS_SEPARATOR);
            for (String currWord : words) {
                if (acceptSentimentWord(currWord)) {

                    double sentimentScoreResult = calculateSentimentScoreForWord(currWord.toLowerCase());
                    sentimentScore.put(currWord.toLowerCase(), sentimentScoreResult);
                    updateWordCount(currWord);
                }
            }
        }
    }

    private List<String> findFirstNElementsInList(List<Map.Entry<String, Double>> sortedEntry, int n) {
        List<String> result = new ArrayList<>();
        int counter = 0;

        for (var curr : sortedEntry) {
            if (counter < n) {
                result.add(curr.getKey());
                counter++;
            }
        }

        return result;
    }

    private void updateAfterAppending(String review) {
        String[] words = review.strip().toLowerCase().split(WORDS_SEPARATOR);

        for (String currWord : words) {
            if (sentimentScore.containsKey(currWord.toLowerCase()) && !stopWords.contains(currWord) &&
                    isValidWord(currWord)) {

                double newSentimentScore = calculateSentimentScoreForWord(currWord);

                sentimentScore.put(currWord.toLowerCase(), newSentimentScore);
                updateWordCount(currWord);
            } else if (acceptSentimentWord(currWord)) {

                double sentimentScoreValue = calculateSentimentScoreForWord(currWord);

                sentimentScore.put(currWord.toLowerCase(), sentimentScoreValue);
                updateWordCount(currWord);
            }
        }
    }
}
