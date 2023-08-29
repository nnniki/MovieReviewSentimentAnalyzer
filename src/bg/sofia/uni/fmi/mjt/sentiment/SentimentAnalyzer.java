package bg.sofia.uni.fmi.mjt.sentiment;

import java.util.List;

public interface SentimentAnalyzer {

    double getReviewSentiment(String review);

    String getReviewSentimentAsName(String review);

    double getWordSentiment(String word);

    int getWordFrequency(String word);

    List<String> getMostFrequentWords(int n);

    List<String> getMostPositiveWords(int n);

    List<String> getMostNegativeWords(int n);

    boolean appendReview(String review, int sentiment);

    int getSentimentDictionarySize();

    boolean isStopWord(String word);

}