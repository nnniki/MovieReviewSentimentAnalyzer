package bg.sofia.uni.fmi.mjt.sentiment;

public record Content(Rating rating, String review) {
    private static final int REVIEW_BEGINNING_INDEX = 2;

    public static Content of(String line) {
        int ratingValue = Integer.parseInt(String.valueOf(line.charAt(0)).strip());
        Rating rating = Rating.getRatingFromValue(ratingValue);
        String review = line.substring(REVIEW_BEGINNING_INDEX).strip().toLowerCase();

        return new Content(rating, review);
    }
}

