package com.medical.service.ocr;

public enum ResultStatus {
    AUTO(0, "自动通过，可直接入库"),
    VERIFY(1, "需要人工确认"),
    LLM(2, "AI修正");

    private final int code;
    private final String description;

    ResultStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ResultStatus fromScore(double score) {
        if (score >= 0.92) return AUTO;
        if (score >= 0.75) return VERIFY;
        return LLM;
    }
}
