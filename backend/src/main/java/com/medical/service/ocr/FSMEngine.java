package com.medical.service.ocr;

import java.util.*;
import java.util.stream.Collectors;

public class FSMEngine {

    // ④ GridIndex用left而非centerX
    private static class GridIndex {
        Map<Integer, List<OCRWord>> cells = new HashMap<>();
        int cellSize;

        GridIndex(List<OCRWord> words) {
            double avgHeight = words.stream()
                    .mapToInt(OCRWord::getHeight)
                    .average().orElse(25);
            this.cellSize = Math.max((int) (avgHeight * 2), 20);

            for (OCRWord w : words) {
                int cellX = w.getLeft() / cellSize;      // ④ 改用left
                int cellY = w.getTop() / cellSize;        // ④ 改用top
                int cellKey = cellY * 10000 + cellX;
                cells.computeIfAbsent(cellKey, k -> new ArrayList<>()).add(w);
            }
        }

        List<OCRWord> getNearbyWords(OCRWord word) {
            Set<OCRWord> nearby = new HashSet<>();
            int cellX = word.getLeft() / cellSize;        // ④ 改用left
            int cellY = word.getTop() / cellSize;         // ④ 改用top

            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int key = (cellY + dy) * 10000 + (cellX + dx);
                    List<OCRWord> cellWords = cells.get(key);
                    if (cellWords != null) {
                        nearby.addAll(cellWords);
                    }
                }
            }
            nearby.remove(word);
            return new ArrayList<>(nearby);
        }
    }

    // ① 真正按字段类型用Regex计算FormatScore
    private static double calculateFormatScore(String text, FieldStrategy strategy) {
        if (text == null || text.isEmpty()) return 0;
        if (strategy == null) {
            long cn = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
            return (double) cn / text.length() > 0.5 ? 0.8 : 0.5;
        }

        switch (strategy.getFieldKey()) {
            case "name":
                return text.matches("^[\\u4e00-\\u9fa5]{2,6}$") ? 1.0 : 0.2;
            case "age":
                return text.matches("^\\d{1,3}\\s*(岁|周岁)?$") ? 1.0 : 0.2;
            case "phone":
                return text.matches("^1[3-9]\\d{9}$") ? 1.0 : 0.1;
            case "idCard":
                return text.matches("^\\d{17}[\\dXx]$") ? 1.0 : 0.1;
            case "visitDate":
                return text.matches("^\\d{4}[-/年]\\d{1,2}[-/月]\\d{1,2}[日]?$") ? 1.0 : 0.3;
            case "gender":
                return text.matches("^[男女]$") ? 1.0 : 0.2;
            case "visitNo":
                return text.matches("^\\d{4,12}$") ? 1.0 : 0.4;
            case "doctor":
                return text.matches("^[\\u4e00-\\u9fa5]{2,4}$") ? 1.0 : 0.4;
            case "hospital":
                long cn = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
                return cn >= 3 ? 0.9 : 0.3;
            case "department":
                cn = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
                return cn >= 2 ? 0.9 : 0.3;
            default:
                // 长文本字段：中文为主即可
                cn = text.chars().filter(c -> c >= 0x4E00 && c <= 0x9FFF).count();
                return (double) cn / text.length() > 0.5 ? 0.7 : 0.4;
        }
    }

    // 生成右侧候选
    private static List<Candidate> generateRightCandidates(OCRWord keyword, List<OCRWord> nearbyWords,
                                                           Set<OCRWord> usedNodes, FieldStrategy strategy) {
        List<Candidate> candidates = new ArrayList<>();
        int H = keyword.getHeight();
        int keywordRight = keyword.getRight();
        int keywordCenterY = keyword.getCenterY();

        for (OCRWord word : nearbyWords) {
            if (usedNodes.contains(word)) continue;
            if (word.getConfidence() < 0.6) continue;

            int dx = word.getLeft() - keywordRight;
            int dy = Math.abs(word.getCenterY() - keywordCenterY);
            if (dx <= 0 || dx > 8 * H || dy > 0.5 * H) continue;

            double rowScore = 1.0 - (dy / (0.5 * H));
            double distanceScore = 1.0 - (dx / (8.0 * H));
            double formatScore = calculateFormatScore(word.getText(), strategy);

            candidates.add(new Candidate(keyword, word, Direction.RIGHT,
                    rowScore, distanceScore, formatScore, word.getConfidence()));
        }
        return candidates;
    }

    // 生成下方候选
    private static List<Candidate> generateBelowCandidates(OCRWord keyword, List<OCRWord> nearbyWords,
                                                           Set<OCRWord> usedNodes, FieldStrategy strategy) {
        List<Candidate> candidates = new ArrayList<>();
        int H = keyword.getHeight();
        int keywordCenterX = keyword.getCenterX();
        int keywordBottom = keyword.getBottom();

        for (OCRWord word : nearbyWords) {
            if (usedNodes.contains(word)) continue;
            if (word.getConfidence() < 0.6) continue;

            int dx = Math.abs(word.getCenterX() - keywordCenterX);
            int dy = word.getTop() - keywordBottom;
            if (dx > H || dy <= 0 || dy > 2.5 * H) continue;

            double rowScore = 0.8 - Math.min(dx / (2.0 * H), 0.8);
            double distanceScore = 1.0 - (dy / (2.5 * H));
            double formatScore = calculateFormatScore(word.getText(), strategy);

            candidates.add(new Candidate(keyword, word, Direction.BELOW,
                    rowScore, distanceScore, formatScore, word.getConfidence()));
        }
        return candidates;
    }

    // ③ Merge前先排序nearbyWords + 遇新关键词停止 + 空白距离停止
    private static List<Candidate> generateMergeCandidates(OCRWord keyword, List<OCRWord> nearbyWords,
                                                           Set<OCRWord> usedNodes, FieldStrategy strategy) {
        int H = keyword.getHeight();
        int keywordCenterX = keyword.getCenterX();
        int keywordBottom = keyword.getBottom();

        // ③ Merge前先按top排序
        List<OCRWord> sortedNearby = new ArrayList<>(nearbyWords);
        sortedNearby.sort(Comparator.comparingInt(OCRWord::getTop));

        List<OCRWord> matchedWords = new ArrayList<>();
        OCRWord lastWord = null;

        for (OCRWord word : sortedNearby) {
            if (usedNodes.contains(word)) continue;
            if (word.getConfidence() < 0.6) continue;

            int dx = Math.abs(word.getCenterX() - keywordCenterX);
            int dy = word.getTop() - keywordBottom;

            if (dx <= H && dy >= 0 && dy <= 1.8 * H) {
                if (isNewKeyword(word.getText())) break;
                if (lastWord != null) {
                    int gap = word.getTop() - lastWord.getBottom();
                    if (gap > 2.5 * H) break;
                }
                matchedWords.add(word);
                lastWord = word;
            }
        }

        if (matchedWords.isEmpty()) return Collections.emptyList();

        double avgConfidence = matchedWords.stream()
                .mapToDouble(OCRWord::getConfidence)
                .average().orElse(0.8);
        double avgFormat = matchedWords.stream()
                .mapToDouble(w -> calculateFormatScore(w.getText(), strategy))
                .average().orElse(0.5);

        List<Candidate> result = new ArrayList<>();
        result.add(new Candidate(keyword, matchedWords, Direction.MERGE,
                0.7, 0.8, avgFormat, avgConfidence));
        return result;
    }

    // ② ⑤ RegexFilter：用FieldStrategy直接判断，不再用字符串displayName
    private static List<Candidate> regexFilter(List<Candidate> candidates, FieldStrategy strategy) {
        if (strategy == null) return candidates;
        String fieldKey = strategy.getFieldKey();

        return candidates.stream()
                .filter(c -> {
                    String text = c.getRawText();

                    switch (fieldKey) {
                        case "name":
                            long cn = text.chars().filter(ch -> ch >= 0x4E00 && ch <= 0x9FFF).count();
                            return cn >= 2 && cn <= 6 && cn == text.length();
                        case "age":
                            return text.matches(".*\\d+.*");
                        case "phone":
                            return text.matches(".*1[3-9]\\d{9}.*");
                        case "gender":
                            return text.contains("男") || text.contains("女");
                        case "visitDate":
                            return text.matches(".*\\d{4}[-/年]\\d{1,2}[-/月]\\d{1,2}.*");
                        case "idCard":
                            return text.matches(".*\\d{17}[\\dXx].*");
                        case "doctor":
                            cn = text.chars().filter(ch -> ch >= 0x4E00 && ch <= 0x9FFF).count();
                            return cn >= 2;
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    // ① 统一评分 + ⑧ preferredBonus归一化
    private static FieldResult scoreCandidate(Candidate c, FieldStrategy strategy) {
        Direction dir = c.getDirection();
        double directionScore = dir.getDefaultScore();
        double preferredBonus = strategy.getPreferredBonus(dir.name());

        double score = ScoringWeights.DEFAULT.calculate(
                c.getRowScore(),
                c.getDistanceScore(),
                c.getAvgConfidence(),   // ⑥ 使用平均置信度
                c.getFormatScore(),
                directionScore
        ) + preferredBonus;

        // ⑧ 归一化到[0,1]
        score = Math.min(score, 1.0);

        String matchType = dir.name();
        String value = c.getRawText();
        OCRWord keyword = c.getKeywordWord();
        OCRWord valueWord = c.getValueWord();
        List<OCRWord> mergeWords = c.getMergeWords();

        // ⑨ 增加ScoreExplain
        Map<String, Double> scoreItems = new LinkedHashMap<>();
        scoreItems.put("Row", c.getRowScore());
        scoreItems.put("Distance", c.getDistanceScore());
        scoreItems.put("OCR", c.getAvgConfidence());
        scoreItems.put("Format", c.getFormatScore());
        scoreItems.put("Direction", directionScore);
        scoreItems.put("Bonus", preferredBonus);

        FieldResult fr;
        if (c.isMerge()) {
            fr = new FieldResult(strategy.getDisplayName(), value, score,
                    "FSM", score < 0.85, matchType, keyword, valueWord, mergeWords);
        } else {
            fr = new FieldResult(strategy.getDisplayName(), value, score,
                    "FSM", score < 0.85, matchType, keyword, valueWord);
        }
        fr.setStatus(ResultStatus.fromScore(score));
        fr.setScoreItems(scoreItems);
        return fr;
    }

    public static List<FieldResult> match(List<OCRWord> words) {
        List<OCRWord> filteredWords = words.stream()
                .filter(w -> w.getConfidence() >= 0.6)
                .collect(Collectors.toList());
        if (filteredWords.isEmpty()) return Collections.emptyList();

        GridIndex grid = new GridIndex(filteredWords);
        Set<OCRWord> usedNodes = new HashSet<>();
        List<FieldResult> results = new ArrayList<>();

        List<FieldStrategy> highPriority = Arrays.stream(FieldStrategy.values())
                .filter(s -> s.getPriority() == FieldStrategy.MatchPriority.HIGH)
                .collect(Collectors.toList());
        List<FieldStrategy> lowPriority = Arrays.stream(FieldStrategy.values())
                .filter(s -> s.getPriority() == FieldStrategy.MatchPriority.LOW)
                .collect(Collectors.toList());

        processStrategies(highPriority, filteredWords, grid, usedNodes, results);
        processStrategies(lowPriority, filteredWords, grid, usedNodes, results);

        return results;
    }

    private static void processStrategies(List<FieldStrategy> strategies, List<OCRWord> words,
                                          GridIndex grid, Set<OCRWord> usedNodes,
                                          List<FieldResult> results) {
        for (FieldStrategy strategy : strategies) {
            for (OCRWord word : words) {
                if (usedNodes.contains(word)) continue;

                String text = word.getText();
                boolean matched = false;
                for (String kw : strategy.getKeywords()) {
                    if (text.contains(kw)) { matched = true; break; }
                }
                if (!matched) continue;

                List<OCRWord> nearbyWords = grid.getNearbyWords(word);

                // 生成所有候选（⑤ 直接传入strategy）
                List<Candidate> allCandidates = new ArrayList<>();
                EnumSet<Direction> dirs = strategy.getMatchDirections();

                if (dirs.contains(Direction.RIGHT)) {
                    allCandidates.addAll(generateRightCandidates(word, nearbyWords, usedNodes, strategy));
                }
                if (dirs.contains(Direction.BELOW)) {
                    allCandidates.addAll(generateBelowCandidates(word, nearbyWords, usedNodes, strategy));
                }
                if (strategy.isAllowMerge()) {
                    allCandidates.addAll(generateMergeCandidates(word, nearbyWords, usedNodes, strategy));
                }

                if (allCandidates.isEmpty()) continue;

                // ⑤ RegexValidator：直接传入strategy
                List<Candidate> filtered = regexFilter(allCandidates, strategy);
                if (filtered.isEmpty()) continue;

                // 统一评分选最优
                FieldResult best = null;
                double bestScore = -1;
                for (Candidate c : filtered) {
                    FieldResult fr = scoreCandidate(c, strategy);
                    if (fr.getScore() > bestScore) {
                        bestScore = fr.getScore();
                        best = fr;
                    }
                }

                if (best != null) {
                    if ("MERGE".equals(best.getMatchType()) && best.getMergeWords() != null) {
                        for (OCRWord mw : best.getMergeWords()) usedNodes.add(mw);
                    } else if (best.getValueWord() != null) {
                        usedNodes.add(best.getValueWord());
                    }
                    results.add(best);
                    usedNodes.add(word);
                }
            }
        }
    }

    private static boolean isNewKeyword(String text) {
        if (text == null || text.isEmpty()) return false;
        for (FieldStrategy s : FieldStrategy.values()) {
            for (String kw : s.getKeywords()) {
                if (text.contains(kw)) return true;
            }
        }
        return false;
    }
}
