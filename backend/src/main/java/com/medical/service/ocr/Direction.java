package com.medical.service.ocr;

public enum Direction {
    RIGHT(1.0),
    BELOW(0.8),
    RIGHT_BELOW(0.9),
    MERGE(0.7);

    private final double defaultScore;

    Direction(double defaultScore) {
        this.defaultScore = defaultScore;
    }

    public double getDefaultScore() {
        return defaultScore;
    }

    public static Direction fromString(String s) {
        if (s == null) return MERGE;
        switch (s.toUpperCase()) {
            case "RIGHT": return RIGHT;
            case "BELOW": return BELOW;
            case "RIGHT_BELOW": return RIGHT_BELOW;
            case "MERGE": return MERGE;
            default: return MERGE;
        }
    }
}
