package com.medical.service.ocr;

import java.util.LinkedHashMap;
import java.util.Map;

public class OCRNormalize {
    private static final Map<String, String> ERROR_DICT = new LinkedHashMap<>();

    static {
        // 中文间多余空格
        ERROR_DICT.put("主 诉", "主诉");
        ERROR_DICT.put("现 病 史", "现病史");
        ERROR_DICT.put("既 往 史", "既往史");
        ERROR_DICT.put("体 格 检 查", "体格检查");
        ERROR_DICT.put("辅 助 检 查", "辅助检查");
        ERROR_DICT.put("处 理 意 见", "处理意见");
        ERROR_DICT.put("诊 断", "诊断");
        ERROR_DICT.put("药 品", "药品");
        ERROR_DICT.put("用 法", "用法");
        ERROR_DICT.put("用 量", "用量");
        ERROR_DICT.put("年 龄", "年龄");

        // OCR形近字错误（字体识别错误）
        ERROR_DICT.put("年岭", "年龄");
        ERROR_DICT.put("药勿", "药物");
        ERROR_DICT.put("主 訴", "主诉");
        ERROR_DICT.put("既 往", "既往");
        ERROR_DICT.put("體检", "体检");
        ERROR_DICT.put("檢查", "检查");
        ERROR_DICT.put("輔助", "辅助");

        // 常见不规范写法
        ERROR_DICT.put("入院日期", "日期");
        ERROR_DICT.put("出院日期", "日期");

        // ⑤ 繁体转简体
        ERROR_DICT.put("現病史", "现病史");
        ERROR_DICT.put("現 病 史", "现病史");
        ERROR_DICT.put("主訴", "主诉");
        ERROR_DICT.put("主 訴", "主诉");
        ERROR_DICT.put("既往史", "既往史");
        ERROR_DICT.put("頭暈", "头晕");
        ERROR_DICT.put("噁心", "恶心");
        ERROR_DICT.put("嘔吐", "呕吐");
        ERROR_DICT.put("發熱", "发热");
        ERROR_DICT.put("發烧", "发烧");
        ERROR_DICT.put("咳嗽", "咳嗽");
        ERROR_DICT.put("醫生", "医生");
        ERROR_DICT.put("體溫", "体温");
        ERROR_DICT.put("脈搏", "脉搏");
        ERROR_DICT.put("血壓", "血压");
        ERROR_DICT.put("過敏", "过敏");
        ERROR_DICT.put("藥物", "药物");

        // OCR常见错误
        ERROR_DICT.put("病 名", "诊断");
        ERROR_DICT.put("病名", "诊断");
        ERROR_DICT.put("診斷", "诊断");
        ERROR_DICT.put("診 斷", "诊断");
    }

    /**
     * 对单个OCR词进行清洗
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) return text;

        String result = text.trim();

        // 1. 去除中文之间的多余空格（保留英文单词间空格）
        StringBuilder sb = new StringBuilder();
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == ' ') {
                // 空格前后都是中文 → 删除空格
                boolean prevIsChinese = i > 0 && isChinese(chars[i - 1]);
                boolean nextIsChinese = i < chars.length - 1 && isChinese(chars[i + 1]);
                if (prevIsChinese && nextIsChinese) {
                    continue; // 跳过这个空格
                }
            }
            sb.append(c);
        }
        result = sb.toString();

        // 2. 错误字典替换
        for (Map.Entry<String, String> entry : ERROR_DICT.entrySet()) {
            String wrong = entry.getKey();
            String correct = entry.getValue();

            // 只替换中文相关的错误，不破坏英文/数字内容
            if (result.contains(wrong)) {
                result = result.replace(wrong, correct);
            }
        }

        return result;
    }

    /**
     * 对整个OCR词列表进行清洗
     */
    public static void normalizeWords(java.util.List<OCRWord> words) {
        for (OCRWord word : words) {
            String originalText = word.getText();
            String normalizedText = normalize(originalText);
            if (!normalizedText.equals(originalText)) {
                word.setText(normalizedText);
            }
        }
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A;
    }
}
