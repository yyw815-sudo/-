package com.medical.service.ocr;

public class ScoringWeights {
    private double rowWeight;
    private double distanceWeight;
    private double ocrWeight;
    private double formatWeight;
    private double directionWeight;

    public static final ScoringWeights DEFAULT = new ScoringWeights(0.25, 0.25, 0.20, 0.20, 0.10);

    public static final ScoringWeights PRIORITY_HIGH = new ScoringWeights(0.20, 0.15, 0.25, 0.30, 0.10);

    public ScoringWeights(double rowWeight, double distanceWeight,
                          double ocrWeight, double formatWeight,
                          double directionWeight) {
        this.rowWeight = rowWeight;
        this.distanceWeight = distanceWeight;
        this.ocrWeight = ocrWeight;
        this.formatWeight = formatWeight;
        this.directionWeight = directionWeight;
    }

    public double getRowWeight() { return rowWeight; }
    public double getDistanceWeight() { return distanceWeight; }
    public double getOcrWeight() { return ocrWeight; }
    public double getFormatWeight() { return formatWeight; }
    public double getDirectionWeight() { return directionWeight; }

    public double calculate(double rowScore, double distanceScore,
                            double ocrConfidence, double formatScore,
                            double dirScore) {
        return rowWeight * rowScore
             + distanceWeight * distanceScore
             + ocrWeight * ocrConfidence
             + formatWeight * formatScore
             + directionWeight * dirScore;
    }

    public double sum() {
        return rowWeight + distanceWeight + ocrWeight + formatWeight + directionWeight;
    }
}
