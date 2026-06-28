package com.medical.service.ocr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldLocator {
    private static final Pattern COLON_PATTERN = Pattern.compile("([^：:]+)[：:]\\s*(.*)");
    private static final Pattern KEYWORD_COLON_PATTERN = Pattern.compile("([^：:\\s]+)[：:]");

    public static class FieldMatch {
        private String fieldName;
        private String value;
        private OCRWord keywordWord;
        private OCRWord valueWord;
        private boolean hasColon;

        public FieldMatch(String fieldName, String value, OCRWord keywordWord, OCRWord valueWord, boolean hasColon) {
            this.fieldName = fieldName;
            this.value = value;
            this.keywordWord = keywordWord;
            this.valueWord = valueWord;
            this.hasColon = hasColon;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getValue() {
            return value;
        }

        public OCRWord getKeywordWord() {
            return keywordWord;
        }

        public OCRWord getValueWord() {
            return valueWord;
        }

        public boolean isHasColon() {
            return hasColon;
        }
    }

    private static final String[][] FIELD_KEYWORDS = {
            {"姓名", "病人姓名", "患者姓名", "名"},
            {"性别", "男", "女"},
            {"年龄", "岁数", "岁"},
            {"身份证", "身份证号", "ID号", "证件号"},
            {"电话", "手机", "手机号", "联系电话"},
            {"日期", "时间", "就诊日期", "开方日期", "检查日期"},
            {"门诊号", "就诊号", "病历号", "病案号"},
            {"科室", "科别", "就诊科室"},
            {"诊断", "临床诊断", "印象", "检查诊断", "病理诊断"},
            {"处方", "药品", "药物", "医嘱", "用法", "用药"},
            {"医生", "医师", "主治医生", "开方医生", "主诊医生"},
            {"医院", "医疗机构", "医院名称"},
            {"主诉", "主 诉"},
            {"现病史", "现 病 史"},
            {"既往史", "既 往 史"},
            {"体格检查", "体 检"},
            {"辅助检查", "辅检", "检查结果"},
            {"处理意见", "治疗方案", "处置"}
    };

    private static String matchFieldName(String text) {
        for (String[] keywords : FIELD_KEYWORDS) {
            for (String keyword : keywords) {
                if (text.contains(keyword)) {
                    return keywords[0];
                }
            }
        }
        return null;
    }

    public static List<FieldMatch> locateFields(List<OCRWord> words) {
        List<FieldMatch> matches = new ArrayList<>();

        for (OCRWord word : words) {
            String text = word.getText().trim();
            if (text.isEmpty()) continue;

            String fieldName = matchFieldName(text);
            if (fieldName == null) continue;

            Matcher colonMatcher = COLON_PATTERN.matcher(text);
            if (colonMatcher.matches()) {
                String value = colonMatcher.group(2).trim();
                if (!value.isEmpty()) {
                    matches.add(new FieldMatch(fieldName, value, word, word, true));
                } else {
                    matches.add(new FieldMatch(fieldName, "", word, null, true));
                }
            } else {
                Matcher keywordColonMatcher = KEYWORD_COLON_PATTERN.matcher(text);
                if (keywordColonMatcher.find()) {
                    matches.add(new FieldMatch(fieldName, "", word, null, true));
                } else {
                    matches.add(new FieldMatch(fieldName, "", word, null, false));
                }
            }
        }

        return matches;
    }

    public static String getStandardFieldName(String fieldName) {
        for (String[] keywords : FIELD_KEYWORDS) {
            for (String keyword : keywords) {
                if (fieldName.contains(keyword) || keyword.contains(fieldName)) {
                    return keywords[0];
                }
            }
        }
        return fieldName;
    }
}
