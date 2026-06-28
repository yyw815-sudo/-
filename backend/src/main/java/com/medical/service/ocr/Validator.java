package com.medical.service.ocr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    public static class ValidationResult {
        private String fieldName;
        private String value;
        private boolean valid;
        private String errorMessage;

        public ValidationResult(String fieldName, String value, boolean valid, String errorMessage) {
            this.fieldName = fieldName;
            this.value = value;
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public String getFieldName() {
            return fieldName;
        }

        public String getValue() {
            return value;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern IDCARD_PATTERN = Pattern.compile("[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}[-/]\\d{1,2}[-/]\\d{1,2}");
    private static final Pattern GENDER_PATTERN = Pattern.compile("^(男|女)$");
    private static final Pattern AGE_PATTERN = Pattern.compile("(\\d{1,3})\\s*(岁|周岁)?");

    public static ValidationResult validate(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ValidationResult(fieldName, value, true, "");
        }

        switch (fieldName) {
            case "phone":
                return validatePhone(value);
            case "idCard":
                return validateIdCard(value);
            case "visitDate":
                return validateDate(value);
            case "gender":
                return validateGender(value);
            case "age":
                return validateAge(value);
            default:
                return new ValidationResult(fieldName, value, true, "");
        }
    }

    private static ValidationResult validatePhone(String value) {
        if (PHONE_PATTERN.matcher(value.replaceAll("\\s", "")).matches()) {
            return new ValidationResult("phone", value, true, "");
        }
        return new ValidationResult("phone", value, false, "手机号格式不正确");
    }

    private static ValidationResult validateIdCard(String value) {
        String cleaned = value.replaceAll("\\s", "").toUpperCase();
        if (IDCARD_PATTERN.matcher(cleaned).matches()) {
            return new ValidationResult("idCard", cleaned, true, "");
        }
        return new ValidationResult("idCard", value, false, "身份证号格式不正确");
    }

    private static ValidationResult validateDate(String value) {
        if (!DATE_PATTERN.matcher(value).matches()) {
            return new ValidationResult("visitDate", value, false, "日期格式不正确");
        }

        try {
            String normalized = value.replace("/", "-");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate.parse(normalized, formatter);
            return new ValidationResult("visitDate", normalized, true, "");
        } catch (DateTimeParseException e) {
            return new ValidationResult("visitDate", value, false, "日期格式不正确");
        }
    }

    private static ValidationResult validateGender(String value) {
        if (GENDER_PATTERN.matcher(value.trim()).matches()) {
            return new ValidationResult("gender", value.trim(), true, "");
        }
        return new ValidationResult("gender", value, false, "性别只能是男或女");
    }

    private static ValidationResult validateAge(String value) {
        Matcher matcher = AGE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return new ValidationResult("age", value, false, "年龄格式不正确");
        }

        try {
            int age = Integer.parseInt(matcher.group(1));
            if (age >= 0 && age <= 120) {
                return new ValidationResult("age", String.valueOf(age), true, "");
            }
            return new ValidationResult("age", value, false, "年龄必须在0~120之间");
        } catch (NumberFormatException e) {
            return new ValidationResult("age", value, false, "年龄格式不正确");
        }
    }

    public static List<ValidationResult> validateAll(java.util.Map<String, String> fields) {
        List<ValidationResult> results = new ArrayList<>();

        for (java.util.Map.Entry<String, String> entry : fields.entrySet()) {
            results.add(validate(entry.getKey(), entry.getValue()));
        }

        return results;
    }

    public static boolean hasErrors(List<ValidationResult> results) {
        return results.stream().anyMatch(r -> !r.isValid());
    }
}
