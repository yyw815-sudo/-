package com.medical.service.ocr;

public class OCRWord {
    private String text;
    private int left;
    private int top;
    private int width;
    private int height;
    private double confidence;

    public OCRWord(String text, int left, int top, int width, int height, double confidence) {
        this.text = text;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.confidence = confidence;
    }

    public int getRight() {
        return left + width;
    }

    public int getBottom() {
        return top + height;
    }

    public int getCenterX() {
        return left + width / 2;
    }

    public int getCenterY() {
        return top + height / 2;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "OCRWord{" +
                "text='" + text + '\'' +
                ", left=" + left +
                ", top=" + top +
                ", width=" + width +
                ", height=" + height +
                ", centerX=" + getCenterX() +
                ", centerY=" + getCenterY() +
                ", confidence=" + confidence +
                '}';
    }
}
