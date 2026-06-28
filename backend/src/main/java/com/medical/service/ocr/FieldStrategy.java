package com.medical.service.ocr;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public enum FieldStrategy {
    NAME("姓名", "name", MatchPriority.HIGH, true, false, true, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("姓名", "病人姓名", "患者姓名", "名称", "姓")),
    GENDER("性别", "gender", MatchPriority.HIGH, true, false, true, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("性别", "男", "女")),
    AGE("年龄", "age", MatchPriority.HIGH, true, false, true, true,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("年龄", "岁数", "岁", "周岁")),
    PHONE("电话", "phone", MatchPriority.HIGH, true, false, true, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("电话", "手机", "手机号", "联系电话")),
    ID_CARD("身份证", "idCard", MatchPriority.HIGH, true, false, true, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("身份证", "身份证号", "ID号", "证件号")),
    VISIT_DATE("日期", "visitDate", MatchPriority.HIGH, true, false, true, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("日期", "时间", "就诊日期", "开方日期", "检查日期", "报告日期")),
    VISIT_NO("门诊号", "visitNo", MatchPriority.HIGH, true, false, true, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("门诊号", "就诊号", "病历号", "病案号")),
    DEPARTMENT("科室", "department", MatchPriority.HIGH, true, false, false, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("科室", "科别", "就诊科室")),
    CHIEF_COMPLAINT("主诉", "chiefComplaint", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.RIGHT, Direction.BELOW), Direction.BELOW,
            Arrays.asList("主诉", "主 诉")),
    PRESENT_HISTORY("现病史", "presentHistory", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.BELOW), Direction.BELOW,
            Arrays.asList("现病史", "现 病 史")),
    PAST_HISTORY("既往史", "pastHistory", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.BELOW), Direction.BELOW,
            Arrays.asList("既往史", "既 往 史", "过去史")),
    PHYSICAL_EXAM("体格检查", "physicalExam", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.BELOW), Direction.BELOW,
            Arrays.asList("体格检查", "体 检", "体检")),
    AUXILIARY_EXAM("辅助检查", "auxiliaryExam", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.BELOW), Direction.BELOW,
            Arrays.asList("辅助检查", "辅检", "检查结果")),
    DIAGNOSIS("诊断", "diagnosis", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.RIGHT, Direction.BELOW), Direction.MERGE,
            Arrays.asList("诊断", "临床诊断", "印象", "检查诊断", "病理诊断")),
    TREATMENT("处理意见", "treatment", MatchPriority.LOW, false, true, false, true,
            EnumSet.of(Direction.BELOW), Direction.BELOW,
            Arrays.asList("处理意见", "治疗方案", "处置", "建议")),
    DOCTOR("医生", "doctor", MatchPriority.HIGH, true, false, false, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("医生", "医师", "主治医生", "开方医生", "主诊医生")),
    HOSPITAL("医院", "hospital", MatchPriority.HIGH, true, false, false, false,
            EnumSet.of(Direction.RIGHT), Direction.RIGHT,
            Arrays.asList("医院", "医疗机构", "医院名称"));

    public enum MatchPriority {
        HIGH(0), LOW(1);
        private final int order;
        MatchPriority(int order) { this.order = order; }
        public int getOrder() { return order; }
    }

    private final String displayName;
    private final String fieldKey;
    private final MatchPriority priority;
    private final boolean singleLine;
    private final boolean allowMerge;
    private final boolean mustRegex;
    private final boolean allowLlm;
    private final EnumSet<Direction> matchDirections;  // ⑦ EnumSet替代String
    private final Direction preferredDirection;
    private final List<String> keywords;

    FieldStrategy(String displayName, String fieldKey, MatchPriority priority,
                  boolean singleLine, boolean allowMerge, boolean mustRegex,
                  boolean allowLlm, EnumSet<Direction> matchDirections,
                  Direction preferredDirection, List<String> keywords) {
        this.displayName = displayName;
        this.fieldKey = fieldKey;
        this.priority = priority;
        this.singleLine = singleLine;
        this.allowMerge = allowMerge;
        this.mustRegex = mustRegex;
        this.allowLlm = allowLlm;
        this.matchDirections = matchDirections;
        this.preferredDirection = preferredDirection;
        this.keywords = keywords;
    }

    public String getDisplayName() { return displayName; }
    public String getFieldKey() { return fieldKey; }
    public MatchPriority getPriority() { return priority; }
    public boolean isSingleLine() { return singleLine; }
    public boolean isAllowMerge() { return allowMerge; }
    public boolean isMustRegex() { return mustRegex; }
    public boolean isAllowLlm() { return allowLlm; }
    public EnumSet<Direction> getMatchDirections() { return matchDirections; }
    public Direction getPreferredDirection() { return preferredDirection; }
    public List<String> getKeywords() { return keywords; }

    // 兼容旧代码：保持getMatchDirection返回String
    public String getMatchDirection() {
        if (matchDirections.contains(Direction.RIGHT) && matchDirections.contains(Direction.BELOW))
            return "RIGHT_BELOW";
        if (matchDirections.contains(Direction.RIGHT)) return "RIGHT";
        if (matchDirections.contains(Direction.BELOW)) return "BELOW";
        return "MERGE";
    }

    public double getPreferredBonus(String actualMatchType) {
        if (preferredDirection == null) return 0;
        if (preferredDirection.name().equals(actualMatchType)) return 0.05;
        if (preferredDirection == Direction.MERGE && "BELOW".equals(actualMatchType)) return 0.03;
        return 0;
    }

    public static FieldStrategy findByDisplayName(String displayName) {
        for (FieldStrategy s : values()) {
            if (s.displayName.equals(displayName)) return s;
        }
        return null;
    }

    public static FieldStrategy findByText(String text) {
        for (FieldStrategy s : values()) {
            for (String kw : s.keywords) {
                if (text.contains(kw)) return s;
            }
        }
        return null;
    }

    public static FieldStrategy findByFieldKey(String fieldKey) {
        for (FieldStrategy s : values()) {
            if (s.fieldKey.equals(fieldKey) || s.displayName.equals(fieldKey)) return s;
        }
        return null;
    }

    public static double getDirectionScore(String direction) {
        switch (direction) {
            case "RIGHT": return 1.0;
            case "BELOW": return 0.8;
            case "RIGHT_BELOW": return 0.9;
            case "MERGE": return 0.7;
            default: return 0.5;
        }
    }
}
