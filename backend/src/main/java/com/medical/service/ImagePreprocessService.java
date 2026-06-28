package com.medical.service;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * 图片预处理服务 - 使用OpenCV优化图片质量，提升OCR识别率
 */
@Service
public class ImagePreprocessService {

    private static final Logger log = LoggerFactory.getLogger(ImagePreprocessService.class);

    @PostConstruct
    public void init() {
        try {
            // 加载OpenCV原生库
            nu.pattern.OpenCV.loadLocally();
            log.info("OpenCV库加载成功，版本: {}", Core.VERSION);
        } catch (Exception e) {
            log.warn("OpenCV库加载失败，将跳过图片预处理: {}", e.getMessage());
        }
    }

    /**
     * 预处理图片，优化OCR识别效果
     * 处理流程：灰度化 → 降噪 → 锐化 → 二值化
     * 
     * @param imageBytes 原始图片字节
     * @return 预处理后的图片字节
     */
    public byte[] preprocessForOcr(byte[] imageBytes) {
        if (!isOpenCvLoaded()) {
            log.warn("OpenCV未加载，跳过预处理");
            return imageBytes;
        }

        try {
            // 加载图片
            Mat original = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
            if (original.empty()) {
                log.warn("图片加载失败，返回原图");
                return imageBytes;
            }

            log.info("开始图片预处理，原始尺寸: {}x{}", original.width(), original.height());

            Mat processed = new Mat();

            // 1. 灰度化 - 减少颜色干扰，提高文字清晰度
            Mat gray = new Mat();
            Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);
            log.debug("灰度化完成");

            // 2. 高斯模糊降噪 - 减少噪点，平滑图像
            Mat blurred = new Mat();
            Imgproc.GaussianBlur(gray, blurred, new org.opencv.core.Size(3, 3), 0);
            log.debug("降噪完成");

            // 3. 锐化增强 - 提高文字边缘清晰度
            Mat sharpened = new Mat();
            Mat kernel = new Mat(3, 3, org.opencv.core.CvType.CV_8SC1);
            // 锐化核矩阵
            kernel.put(0, 0, 
                0, -1, 0,
                -1, 5, -1,
                0, -1, 0
            );
            Imgproc.filter2D(blurred, sharpened, blurred.depth(), kernel);
            log.debug("锐化完成");

            // 4. 自适应二值化 - 将文字转为黑白，增强对比度
            Mat binary = new Mat();
            Imgproc.adaptiveThreshold(sharpened, binary, 255, 
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, 
                Imgproc.THRESH_BINARY, 
                11, 2);
            log.debug("二值化完成");

            // 保存处理后的图片
            MatOfByte outputBuffer = new MatOfByte();
            Imgcodecs.imencode(".png", binary, outputBuffer);
            byte[] result = outputBuffer.toArray();

            // 释放资源
            original.release();
            gray.release();
            blurred.release();
            sharpened.release();
            kernel.release();
            binary.release();

            log.info("图片预处理完成，输出大小: {} bytes", result.length);
            return result;

        } catch (Exception e) {
            log.error("图片预处理失败: {}", e.getMessage(), e);
            return imageBytes;
        }
    }

    /**
     * 检查OpenCV是否已加载
     */
    public boolean isOpenCvLoaded() {
        try {
            return Core.VERSION != null && !Core.VERSION.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 简化预处理 - 仅灰度化和二值化（适用于高质量图片）
     */
    public byte[] preprocessSimple(byte[] imageBytes) {
        if (!isOpenCvLoaded()) {
            return imageBytes;
        }

        try {
            Mat original = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
            if (original.empty()) {
                return imageBytes;
            }

            Mat gray = new Mat();
            Imgproc.cvtColor(original, gray, Imgproc.COLOR_BGR2GRAY);

            Mat binary = new Mat();
            Imgproc.threshold(gray, binary, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

            MatOfByte outputBuffer = new MatOfByte();
            Imgcodecs.imencode(".png", binary, outputBuffer);
            byte[] result = outputBuffer.toArray();

            original.release();
            gray.release();
            binary.release();

            return result;

        } catch (Exception e) {
            log.error("简化预处理失败: {}", e.getMessage());
            return imageBytes;
        }
    }
}