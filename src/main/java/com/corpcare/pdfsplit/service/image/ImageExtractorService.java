package com.corpcare.pdfsplit.service.image;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

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
            "normal", "arrow", "name", "age", "male", "female"
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

    public String extract(PDFRenderer renderer, int pageIndex) throws IOException {
        BufferedImage original = renderer.renderImageWithDPI(pageIndex, 500);
        BufferedImage bestRotated = findBestRotation(original);
        int w = bestRotated.getWidth();
        int h = bestRotated.getHeight();

        StringBuilder combined = new StringBuilder();
        String z1 = readZone(bestRotated, 0, 0, w, (int) (h * 0.12));
        combined.append(z1);
        String z2 = readZoneThreshold(bestRotated, 0, (int) (h * 0.74), w, (int) (h * 0.92));
        combined.append("\n").append(z2);
        return combined.toString().toLowerCase().trim();
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

    private String readZone(BufferedImage img, int x, int y, int w, int h) {
        try {
            int safeW = Math.min(w, img.getWidth()) - x;
            int safeH = Math.min(h, img.getHeight()) - y;
            if (safeW <= 0 || safeH <= 0) return "";
            BufferedImage crop = img.getSubimage(x, y, safeW, safeH);
            return readWithTesseract(crop);
        } catch (Exception e) {
            return "";
        }
    }

    private String readZoneThreshold(BufferedImage img, int x, int y, int w, int h) {
        try {
            int safeW = Math.min(w, img.getWidth()) - x;
            int safeH = Math.min(h, img.getHeight()) - y;
            if (safeW <= 0 || safeH <= 0) return "";
            BufferedImage crop = img.getSubimage(x, y, safeW, safeH);
            BufferedImage clean = toBlackWhite(crop);
            return readWithTesseract(clean);
        } catch (Exception e) {
            return "";
        }
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

    private BufferedImage rotate(BufferedImage src, int degrees) {
        if (degrees == 0) return src;
        int w = src.getWidth();
        int h = src.getHeight();
        int newW = (degrees == 90 || degrees == 270) ? h : w;
        int newH = (degrees == 90 || degrees == 270) ? w : h;
        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
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

    private BufferedImage toBlackWhite(BufferedImage src) {
        BufferedImage bw = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = bw.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        for (int y = 0; y < bw.getHeight(); y++)
            for (int x = 0; x < bw.getWidth(); x++) {
                int p = bw.getRaster().getSample(x, y, 0);
                bw.getRaster().setSample(x, y, 0, p < 160 ? 0 : 255);
            }
        return bw;
    }
}
