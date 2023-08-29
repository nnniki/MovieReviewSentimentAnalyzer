package bg.sofia.uni.fmi.mjt.sentiment;

public enum Rating {
    unknown(-1),
    negative(0),
    somewhatNegative(1),
    neutral(2),
    somewhatPositive(3),
    positive(4);

    private final int rating;

    Rating(int rating) {
        this.rating = rating;
    }

    public static Rating getRatingFromValue(int value) {
        for (Rating type : values()) {
            if (type.getRating() == value) {
                return type;
            }
        }

        return null;
    }

    int getRating() {
        return rating;
    }
}
