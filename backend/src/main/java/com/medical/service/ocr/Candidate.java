package com.medical.service.ocr;

import java.util.List;
import java.util.stream.Collectors;

public class Candidate {
    private OCRWord keywordWord;
    private OCRWord valueWord;
    private List<OCRWord> mergeWords;
    private Direction direction;
    private double rowScore;
    private double distanceScore;
    private double formatScore;
    private double avgConfidence;   // ⑥
    private String rawText;

    public Candidate(OCRWord keywordWord, OCRWord valueWord, Direction direction,
                     double rowScore, double distanceScore, double formatScore,
                     double avgConfidence) {
        this.keywordWord = keywordWord;
        this.valueWord = valueWord;
        this.direction = direction;
        this.rowScore = rowScore;
        this.distanceScore = distanceScore;
        this.formatScore = formatScore;
        this.avgConfidence = avgConfidence;
        this.rawText = valueWord.getText();
    }

    public Candidate(OCRWord keywordWord, List<OCRWord> mergeWords, Direction direction,
                     double rowScore, double distanceScore, double formatScore,
                     double avgConfidence) {
        this.keywordWord = keywordWord;
        this.mergeWords = mergeWords;
        this.valueWord = mergeWords.get(0);
        this.direction = direction;
        this.rowScore = rowScore;
        this.distanceScore = distanceScore;
        this.formatScore = formatScore;
        this.avgConfidence = avgConfidence;
        this.rawText = mergeWords.stream()
                .map(OCRWord::getText)
                .collect(Collectors.joining(" "));
    }

    public OCRWord getKeywordWord() { return keywordWord; }
    public OCRWord getValueWord() { return valueWord; }
    public List<OCRWord> getMergeWords() { return mergeWords; }
    public Direction getDirection() { return direction; }
    public double getRowScore() { return rowScore; }
    public double getDistanceScore() { return distanceScore; }
    public double getFormatScore() { return formatScore; }
    public double getAvgConfidence() { return avgConfidence; }
    public String getRawText() { return rawText; }
    public boolean isMerge() { return mergeWords != null && !mergeWords.isEmpty(); }
}
