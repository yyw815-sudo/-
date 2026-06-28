package com.medical.service.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexEngine {
    public static class RegexMatch {
        private String fieldName;
        private String value;
        private OCRWord sourceWord;
        private double confidence;

        public RegexMatch(String fieldName, String value, OCRWord sourceWord, double confidence) {
            this.fieldName = fieldName;
            this.value = value;
            this.sourceWord = sourceWord;
            this.confidence = confidence;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getValue() {
            return value;
        }

        public OCRWord getSourceWord() {
            return sourceWord;
        }

        public double getConfidence() {
            return confidence;
        }
    }

    private static class RegexRule {
        String fieldName;
        Pattern pattern;
        int priority;
        double confidence;

        RegexRule(String fieldName, String pattern, int priority, double confidence) {
            this.fieldName = fieldName;
            this.pattern = Pattern.compile(pattern);
            this.priority = priority;
            this.confidence = confidence;
        }
    }

    private static final List<RegexRule> RULES = new ArrayList<>();

    static {
        RULES.add(new RegexRule("phone", "1[3-9]\\d{9}", 1, 0.95));
        RULES.add(new RegexRule("idCard", "[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]", 2, 0.95));
        RULES.add(new RegexRule("visitDate", "\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}", 3, 0.90));
        RULES.add(new RegexRule("age", "(\\d{1,3})\\s*(岁|周岁)", 4, 0.85));
        RULES.add(new RegexRule("visitNo", "(\\d{4,12})", 5, 0.80));
        RULES.add(new RegexRule("gender", "(男|女)", 6, 0.95));
    }

    public static List<RegexMatch> extract(List<OCRWord> words) {
        List<RegexMatch> results = new ArrayList<>();

        for (OCRWord word : words) {
            String text = word.getText();

            for (RegexRule rule : RULES) {
                Matcher matcher = rule.pattern.matcher(text);
                if (matcher.find()) {
                    String value = matcher.group();

                    boolean alreadyExists = results.stream()
                            .anyMatch(r -> r.getFieldName().equals(rule.fieldName) && r.getValue().equals(value));

                    if (!alreadyExists) {
                        double finalConfidence = rule.confidence * word.getConfidence();
                        results.add(new RegexMatch(rule.fieldName, value, word, finalConfidence));
                    }
                }
            }
        }

        results.sort((a, b) -> Integer.compare(
                RULES.stream().filter(r -> r.fieldName.equals(a.getFieldName())).findFirst().map(r -> r.priority).orElse(999),
                RULES.stream().filter(r -> r.fieldName.equals(b.getFieldName())).findFirst().map(r -> r.priority).orElse(999)
        ));

        return results;
    }

    public static String extractSingle(String text, String fieldName) {
        for (RegexRule rule : RULES) {
            if (rule.fieldName.equals(fieldName)) {
                Matcher matcher = rule.pattern.matcher(text);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return null;
    }
}
