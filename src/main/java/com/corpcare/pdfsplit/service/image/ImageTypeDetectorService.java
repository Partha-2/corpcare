package com.corpcare.pdfsplit.service.image;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ImageTypeDetectorService {

    private static final Map<String, List<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("ECG", Arrays.asList(
                "ecg", "electrocardiogram", "heart rate", "qrs", "bpm",
                "sinus rhythm", "pr interval", "qtcb", "p-r-t", "rhythm"));

        KEYWORDS.put("XRAY", Arrays.asList(
                "x-ray", "xray", "radiograph", "lung", "opacity", "pleural",
                "thorax", "diaphragm", "trachea", "mediastinum", "consolidation"));

        KEYWORDS.put("MRI", Arrays.asList(
                "mri", "magnetic resonance", "brain", "lesion", "signal",
                "contrast", "t1", "t2", "flair", "axial", "sagittal"));

        KEYWORDS.put("USG", Arrays.asList(
                "ultrasound", "usg", "sonography", "liver", "gallbladder", "spleen",
                "kidney size", "echo", "abdomen", "pelvis", "foetal"));

        KEYWORDS.put("PRESCRIPTION", Arrays.asList(
                "tablet", "capsule", "mg", "dosage", "prescribed", "medicine",
                "once daily", "twice daily", "syrup", "injection", "rx"));
    }

    public String detect(String text) {
        if (text == null || text.isEmpty()) return "UNKNOWN";
        String bestType = "UNKNOWN";
        int highestScore = 0;
        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            String type = entry.getKey();
            List<String> words = entry.getValue();
            int score = 0;
            for (String word : words) {
                if (text.contains(word)) score++;
            }
            if (score > highestScore) {
                highestScore = score;
                bestType = type;
            }
        }
        return bestType;
    }

    public String confidence(String text, String type) {
        if (type.equals("UNKNOWN")) return "LOW";
        List<String> words = KEYWORDS.get(type);
        if (words == null) return "LOW";
        int score = 0;
        for (String word : words) {
            if (text.contains(word)) score++;
        }
        if (score >= 3) return "HIGH";
        if (score == 2) return "MEDIUM";
        return "LOW";
    }
}
