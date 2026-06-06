package com.corpcare.pdfsplit.service.pdf;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PdfCategoryDetector {

    private static final Map<String, List<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("lab_blood", Arrays.asList(
                "blood", "haemoglobin", "hemoglobin",
                "rbc", "wbc", "platelet", "cbc",
                "serum", "glucose", "cholesterol",
                "triglyceride", "hba1c", "thyroid", "tsh"));

        KEYWORDS.put("lab_urine", Arrays.asList(
                "urine", "urinalysis", "urea",
                "creatinine", "kidney", "renal",
                "albumin", "nitrite", "leucocyte"));

        KEYWORDS.put("eye", Arrays.asList(
                "eye", "vision", "ophthalmology",
                "retina", "cornea", "visual acuity",
                "cataract", "glaucoma", "ocular"));

        KEYWORDS.put("chest", Arrays.asList(
                "chest", "lung", "pulmonary",
                "xray", "x-ray", "cardiac", "heart",
                "radiograph", "thorax", "ecg"));
    }

    public String toFinal(String raw) {
        if (raw == null) return "chest";
        if (raw.startsWith("lab")) return "lab";
        return raw;
    }

    public String detectOne(String text) {
        if (text == null || text.isEmpty()) return null;

        String bestCategory = null;
        int highestScore = 0;

        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            String category = entry.getKey();
            List<String> words = entry.getValue();
            int score = 0;
            for (String word : words) {
                if (text.contains(word)) score++;
            }
            if (score > highestScore) {
                highestScore = score;
                bestCategory = category;
            }
        }
        return bestCategory;
    }

    public List<String> detectAll(List<String> pageTexts) {
        List<String> categories = new ArrayList<>();
        for (String text : pageTexts) {
            categories.add(detectOne(text));
        }
        resolveUndetected(categories);
        return categories;
    }

    private void resolveUndetected(List<String> categories) {
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i) != null) continue;
            String resolved;
            boolean prevExists = i > 0 && categories.get(i - 1) != null;
            boolean nextExists = i < categories.size() - 1 && categories.get(i + 1) != null;
            if (prevExists) {
                resolved = categories.get(i - 1);
            } else if (nextExists) {
                resolved = categories.get(i + 1);
            } else {
                resolved = "chest";
            }
            categories.set(i, resolved);
        }
    }
}
