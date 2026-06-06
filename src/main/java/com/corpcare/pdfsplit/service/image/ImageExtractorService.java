package com.corpcare.pdfsplit.service.image;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

@Service
public class ImageExtractorService {

    @Value("${ocr.tesseract.datapath:/usr/share/tessdata}")
    private String tessDataPath;

    private Tesseract tesseract;

    private static final String[] SCORE_KEYWORDS = {
            "ecg", "xray", "x-ray", "blood", "urine", "eye",
            "chest", "lung", "patient", "report", "hr", "rr",
            "qrs", "bpm", "ms", "rhythm", "diagnosis", "mg",
            "tablet", "mri", "ultrasound", "usg", "sinus",
            "normal", "arrow", "name", "age", "male", "female",
            "heart", "liver", "kidney", "vision", "cataract"
    };

    private synchronized Tesseract getTesseract() {
        if (tesseract == null) {
            tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setOcrEngineMode(1);
            tesseract.setPageSegMode(6);
        }
        return tesseract;
    }

    public String extract(PDFRenderer renderer, int pageIndex) {
        try {
            BufferedImage original = renderer.renderImageWithDPI(pageIndex, 500);

            BufferedImage gray = toGrayscale(original);

            BufferedImage oriented = findBestRotation(gray);

            BufferedImage deskewed = deskew(oriented);

            BufferedImage thresholded = applyOtsuThreshold(deskewed);

            String text = readWithTesseract(thresholded);
            return text.toLowerCase().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = gray.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage findBestRotation(BufferedImage src) {
        BufferedImage best = src;
        int highestScore = 0;
        for (int rotation : new int[]{0, 90, 180, 270}) {
            BufferedImage rotated = rotate(src, rotation);
            String text = readWithTesseract(rotated);
            int score = score(text);
            if (score > highestScore) {
                highestScore = score;
                best = rotated;
            }
        }
        return best;
    }

    private BufferedImage deskew(BufferedImage src) {
        int sw = 400;
        int sh = (int) (src.getHeight() * ((double) sw / src.getWidth()));
        BufferedImage small = new BufferedImage(sw, sh, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gs = small.createGraphics();
        gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gs.drawImage(src, 0, 0, sw, sh, null);
        gs.dispose();

        int threshold = otsuThreshold(small);
        for (int y = 0; y < sh; y++)
            for (int x = 0; x < sw; x++)
                small.getRaster().setSample(x, y, 0,
                        small.getRaster().getSample(x, y, 0) < threshold ? 0 : 255);

        double bestAngle = 0;
        double bestVariance = 0;
        for (double angle = -3; angle <= 3.01; angle += 0.5) {
            BufferedImage rotated = rotateDouble(small, angle);
            double variance = horizontalProjectionVariance(rotated);
            if (variance > bestVariance) {
                bestVariance = variance;
                bestAngle = angle;
            }
        }

        if (Math.abs(bestAngle) < 0.5) return src;
        return rotateDouble(src, bestAngle);
    }

    private double horizontalProjectionVariance(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        long[] projection = new long[h];
        for (int y = 0; y < h; y++) {
            long sum = 0;
            for (int x = 0; x < w; x++) {
                sum += img.getRaster().getSample(x, y, 0);
            }
            projection[y] = sum;
        }
        double mean = 0;
        for (int y = 0; y < h; y++) mean += projection[y];
        mean /= h;
        double variance = 0;
        for (int y = 0; y < h; y++) {
            double diff = projection[y] - mean;
            variance += diff * diff;
        }
        return variance;
    }

    private BufferedImage applyOtsuThreshold(BufferedImage gray) {
        int threshold = otsuThreshold(gray);
        BufferedImage bw = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < gray.getHeight(); y++)
            for (int x = 0; x < gray.getWidth(); x++) {
                int p = gray.getRaster().getSample(x, y, 0);
                bw.getRaster().setSample(x, y, 0, p < threshold ? 0 : 255);
            }
        return bw;
    }

    private int otsuThreshold(BufferedImage gray) {
        int[] histogram = new int[256];
        int w = gray.getWidth();
        int h = gray.getHeight();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int p = gray.getRaster().getSample(x, y, 0);
                histogram[Math.max(0, Math.min(255, p))]++;
            }
        int total = w * h;
        float sum = 0;
        for (int i = 0; i < 256; i++) sum += i * histogram[i];
        float sumB = 0;
        int wB = 0;
        int wF;
        float maxVariance = 0;
        int threshold = 128;
        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;
            if (wF == 0) break;
            sumB += (float) i * histogram[i];
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;
            float diff = mB - mF;
            float variance = (float) wB * (float) wF * diff * diff;
            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }
        return threshold;
    }

    private BufferedImage rotateDouble(BufferedImage src, double degrees) {
        double rad = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(rad));
        double cos = Math.abs(Math.cos(rad));
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (int) (w * cos + h * sin);
        int newH = (int) (w * sin + h * cos);
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, newW, newH);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate((newW - w) / 2.0, (newH - h) / 2.0);
        at.rotate(rad, w / 2.0, h / 2.0);
        g.drawImage(src, at, null);
        g.dispose();
        return out;
    }

    private BufferedImage rotate(BufferedImage src, int degrees) {
        if (degrees == 0) return src;
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (degrees == 90 || degrees == 270) ? h : w;
        int newH = (degrees == 90 || degrees == 270) ? w : h;
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, newW, newH);
        g.translate(newW / 2.0, newH / 2.0);
        g.rotate(Math.toRadians(degrees));
        g.translate(-w / 2.0, -h / 2.0);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    private synchronized String readWithTesseract(BufferedImage image) {
        try {
            return getTesseract().doOCR(image).toLowerCase().trim();
        } catch (TesseractException e) {
            return "";
        }
    }

    private int score(String text) {
        if (text == null || text.isEmpty()) return 0;
        int count = 0;
        for (String kw : SCORE_KEYWORDS) {
            if (text.contains(kw)) count++;
        }
        return count;
    }
}
