package bg.sofia.uni.fmi.mjt.sentiment;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class MovieReviewSentimentAnalyzerTest {

    private static final String stopWordsFile = "stopwords.txt";
    private Reader readStopWords;
    private Reader readReview;
    private Writer writeReview;
    private MovieReviewSentimentAnalyzer analyzer;

    String test = "1 A series of escapades demonstrating the adage that what is GOOD for the goose is also good for the " +
            "gander , some of which occasionally amuses but none of which amounts to much of a story ." + System.lineSeparator() +
            "4 It is most remarkable not because of its epic scope , but because of the startling intimacy it achieves despite that breadth ." + System.lineSeparator() +
            "3 Instead , she sees it as a chance to revitalize what is and always has been remarkable about clung-to traditions ." + System.lineSeparator() +
            "4 Time is a beautiful film to watch , an interesting and at times captivating take on loss and loneliness ." + System.lineSeparator() +
            "1 Predecessors The Mummy and The Mummy Returns stand as intellectual masterpieces next to The Scorpion King ." + System.lineSeparator() +
            "4 Time is a beautiful film to watch , an interesting and at tIMes/\\captivating take on loss and loneliness ." + System.lineSeparator() +
            "2 Oops , she's really done it good this time ." + System.lineSeparator() +
            "0 Kirshner and Monroe seem to be in a contest to see who can out-bad-act the other ." + System.lineSeparator();

    @BeforeEach
    void setTestData() throws IOException {
        readStopWords = new FileReader(stopWordsFile);
        readReview = new StringReader(test);
        writeReview = new StringWriter();
        analyzer = new MovieReviewSentimentAnalyzer(readStopWords, readReview, writeReview);
    }

    @AfterEach
    void closeStreams() throws IOException {
        readStopWords.close();
        readReview.close();
        writeReview.close();
    }

    @Test
    void testGetWordSentimentSuccessfully() {

        assertEquals(3.333, analyzer.getWordSentiment("TiMe"),3,
                "Error: Result is not the same as expected, note that words are considered case-insensitive");
    }

    @Test
    void testGetWordSentimentMissingWord() {
        assertEquals(-1.0, analyzer.getWordSentiment("Football"),1,
                "Error: Result is not the same as expected, missing words has sentiment equals -1.0");
    }

    @Test
    void testGetWordSentimentStopWord() {
        assertEquals(-1.0, analyzer.getWordSentiment("is"),1,
                "Error: Result is not the same as expected, stop words has sentiment equals -1.0");
    }

    @Test
    void testGetReviewSentimentSuccessfully() {
        assertEquals(3.833, analyzer.getReviewSentiment("This film was one of the most remarkable and interesting i've watched so far"),3,
                "Error: Result is not the same as expected, note that missing and stop words shouldn't be included");
    }

    @Test
    void testGetReviewSentimentAllWordsAreMissingOrStopWords() {
        assertEquals(-1.0, analyzer.getReviewSentiment("The show for Cristiano Ronaldo is so brilliantly-presented"),3,
                "Error: Result is not the same as expected, if only missing or stop words are presented the result is -1.0");
    }

    @Test
    void testGetReviewSentimentAsNamePositive() {
        assertEquals("positive", analyzer.getReviewSentimentAsName("This film was one of the most remarkable and interesting i've watched so far"),
                "Error: The sentiment's name was expected to be positive but is not");
    }

    @Test
    void testGetReviewSentimentAsNameUnknown() {
        assertEquals("unknown", analyzer.getReviewSentimentAsName("The show for Cristiano Ronaldo is so brilliantly-presented"),
                "Error: The sentiment's name was expected to be unknown but is not");
    }

    @Test
    void testGetWordFrequencySuccessfully() {
        assertEquals(3, analyzer.getWordFrequency("Good"),
                "Error: Result is not the same as expected. Expected result is 3");
    }

    @Test
    void testGetWordFrequencyMissingWord() {
        assertEquals(0, analyzer.getWordFrequency("Cristiano"),
                "Error: Result is not the same as expected. Expected result is 0");
    }

    @Test
    void testGetMostFrequentWordsInvalidN() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getMostFrequentWords(-2),
                "Error: IllegalArgumentException was expected when n is negative");
    }

    @Test
    void testGetMostFrequentWordsSuccessfully() {
        List<String> result = new ArrayList<>();
        result.add("good");
        result.add("time");

        assertTrue(result.containsAll(analyzer.getMostFrequentWords(2)), "Result is not correct, it should contain all words");
        assertTrue(analyzer.getMostFrequentWords(2).containsAll(result), "Result is not correct, it should contain all words");
    }

    @Test
    void testGetMostFrequentWordsZeroN() {
        assertTrue(analyzer.getMostFrequentWords(0).isEmpty(), "Result should be empty list when N is 0");
        assertTrue(analyzer.getMostFrequentWords(0).isEmpty(), "Result should be empty list when N is 0");
    }

    @Test
    void testGetMostPositiveWordsInvalidN() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getMostPositiveWords(-2),
                "IllegalArgumentException was expected when n is negative");
    }

    @Test
    void testGetMostPositiveWordsSuccessfully() {
        List<String> result = new ArrayList<>();
        result.add("breadth");
        result.add("achieves");
        result.add("beautiful");
        result.add("loss");
        result.add("epic");
        result.add("film");
        result.add("take");
        result.add("captivating");
        result.add("loneliness");
        result.add("intimacy");
        result.add("times");
        result.add("scope");
        result.add("interesting");
        result.add("despite");
        result.add("watch");
        result.add("startling");
        result.add("remarkable");

        assertTrue(result.containsAll(analyzer.getMostPositiveWords(17)), "Result is not correct, it should contain all words");
        assertTrue(analyzer.getMostPositiveWords(17).containsAll(result), "Result is not correct, it should contain all words");
    }

    @Test
    void testGetMostNegativeWordsInvalidN() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.getMostNegativeWords(-2),
                "IllegalArgumentException was expected while n is negative");
    }

    @Test
    void testGetMostNegativeWordsSuccessfully() {
        List<String> result = new ArrayList<>();
        result.add("monroe");
        result.add("see");
        result.add("can");
        result.add("seem");
        result.add("kirshner");
        result.add("contest");
        result.add("act");
        result.add("bad");

        assertTrue(result.containsAll(analyzer.getMostNegativeWords(8)), "Result is not correct, it should contain all words");
        assertTrue(analyzer.getMostNegativeWords(8).containsAll(result), "Result is not correct, it should contain all words");
    }

    @Test
    void testIsStopWordSuccessfully() {
        assertTrue(analyzer.isStopWord("the"), "Expected result was true");
    }

    @Test
    void testGetSentimentDictionarySizeSuccessfully() {
        assertEquals(59, analyzer.getSentimentDictionarySize(),
         "Result was expected to be 59 but it isn't, note that stop words and punctuation symbols are not included");
    }

    @Test
    void testAppendReviewNullReview() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.appendReview(null,2),
                "IllegalArgumentException was expected when review is null");
    }

    @Test
    void testAppendReviewOutOfRangeSentiment() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.appendReview("This is a lovely story",8),
                "IllegalArgumentException was expected when sentiment value is below 0 or over 4");
    }

    @Test
    void testAppendReviewAndChangeCountOfWords() {
        assertEquals(3, analyzer.getWordFrequency("Good"),
                "Expected count of word good was 3 but the result was not the same");
        assertEquals(2, analyzer.getWordFrequency("fiLM"),
                "Expected count of word film was 2 but the result was not the same");

        analyzer.appendReview("This film is a film about really good dog",3);
        assertEquals(4, analyzer.getWordFrequency("Good"),
                "Expected count of word good after appending new review is 4 now");
        assertEquals(4, analyzer.getWordFrequency("Film"),
                "Expected count of word film after appending new review is 4 now");
    }

    @Test
    void testGetWordSentimentAfterAppendingReview() {
        assertEquals(3.333, analyzer.getWordSentiment("TiMe"),3,
                "Result is not the same as expected, note that words are considered case-insensitive");

        analyzer.appendReview("This film is one of the greatest of all Time .",3);
        assertEquals(3.25, analyzer.getWordSentiment("TiMe"),3,
                "Expected sentiment score of word time after appending new review is 3.25");
        assertEquals(3.25, analyzer.getWordSentiment("TiMe"),3,
                "Expected sentiment score of word time after appending new review is 3.25");
    }

    @Test
    void testGetWordFrequencyDoubleAppend() {
        assertEquals(3, analyzer.getWordFrequency("Good"),
                "Expected count of word good was 3 but the result was not the same");
        assertEquals(2, analyzer.getWordFrequency("fiLM"),
                "Expected count of word film was 2 but the result was not the same");

        analyzer.appendReview("This film is a film about really good dog",3);
        assertEquals(4, analyzer.getWordFrequency("Good"),
                "Expected count of word good after appending new review is 4 now");
        assertEquals(4, analyzer.getWordFrequency("Film"),
                "Expected count of word film after appending new review is 4 now");

        analyzer.appendReview("This is a really good film for Sunday night",3);
        assertEquals(5, analyzer.getWordFrequency("Good"),
                "Expected count of word good after appending new review is 5 now");
        assertEquals(5, analyzer.getWordFrequency("Film"),
                "Expected count of word film after appending new review is 5 now");
    }
}