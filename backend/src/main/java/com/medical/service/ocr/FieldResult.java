package com.medical.service.ocr;

import java.util.List;
import java.util.Map;

public class FieldResult {
    private String fieldName;
    private String value;
    private double score;
    private String source;         // FSM / REGEX / LLM / FALLBACK
    private boolean needCorrection;
    private String matchType;      // RIGHT / BELOW / MERGE / DIRECT / REGEX / LLM
    private OCRWord keywordWord;
    private OCRWord valueWord;
    private List<OCRWord> mergeWords;
    private ResultStatus status;
    private double fieldConfidence;
    private Map<String, Double> scoreItems; // ⑨ 每维分数详情

    public FieldResult() {
    }

    public FieldResult(String fieldName, String value, double score, String source,
                       boolean needCorrection, String matchType) {
        this.fieldName = fieldName;
        this.value = value;
        this.score = score;
        this.source = source;
        this.needCorrection = needCorrection;
        this.matchType = matchType;
    }

    public FieldResult(String fieldName, String value, double score, String source,
                       boolean needCorrection, String matchType,
                       OCRWord keywordWord, OCRWord valueWord) {
        this(fieldName, value, score, source, needCorrection, matchType);
        this.keywordWord = keywordWord;
        this.valueWord = valueWord;
    }

    public FieldResult(String fieldName, String value, double score, String source,
                       boolean needCorrection, String matchType,
                       OCRWord keywordWord, OCRWord valueWord,
                       List<OCRWord> mergeWords) {
        this(fieldName, value, score, source, needCorrection, matchType, keywordWord, valueWord);
        this.mergeWords = mergeWords;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isNeedCorrection() {
        return needCorrection;
    }

    public void setNeedCorrection(boolean needCorrection) {
        this.needCorrection = needCorrection;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public OCRWord getKeywordWord() {
        return keywordWord;
    }

    public void setKeywordWord(OCRWord keywordWord) {
        this.keywordWord = keywordWord;
    }

    public OCRWord getValueWord() {
        return valueWord;
    }

    public void setValueWord(OCRWord valueWord) {
        this.valueWord = valueWord;
    }

    public List<OCRWord> getMergeWords() {
        return mergeWords;
    }

    public void setMergeWords(List<OCRWord> mergeWords) {
        this.mergeWords = mergeWords;
    }

    public ResultStatus getStatus() {
        return status;
    }

    public void setStatus(ResultStatus status) {
        this.status = status;
    }

    public double getFieldConfidence() {
        return fieldConfidence;
    }

    public void setFieldConfidence(double fieldConfidence) {
        this.fieldConfidence = fieldConfidence;
    }

    public Map<String, Double> getScoreItems() {
        return scoreItems;
    }

    public void setScoreItems(Map<String, Double> scoreItems) {
        this.scoreItems = scoreItems;
    }

    @Override
    public String toString() {
        return "FieldResult{" +
                "fieldName='" + fieldName + '\'' +
                ", value='" + value + '\'' +
                ", score=" + score +
                ", source='" + source + '\'' +
                ", needCorrection=" + needCorrection +
                ", matchType='" + matchType + '\'' +
                '}';
    }
}
