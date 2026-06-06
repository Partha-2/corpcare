package com.corpcare.pdfsplit.service.image;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ImageTypeDetectorService {

    private static final Map<String, List<String>> KEYWORDS = new LinkedHashMap<>();

    static {
        KEYWORDS.put("ECG", Arrays.asList(
                "ecg", "electrocardiogram", "electro cardiogram",
                "heart rate", "qrs", "bpm", "beats per minute",
                "sinus rhythm", "pr interval", "qtcb", "p-r-t",
                "rhythm", "ventricular rate", "atrial rate",
                "qrs duration", "qt interval", "qtc",
                "p wave", "t wave", "st segment", "arrhythmia",
                "lead i", "lead ii", "lead iii", "avl", "avr", "avf",
                "v1", "v2", "v3", "v4", "v5", "v6"));

        KEYWORDS.put("XRAY", Arrays.asList(
                "x-ray", "xray", "radiograph", "radiograph",
                "lung", "opacity", "pleural", "thorax",
                "diaphragm", "trachea", "mediastinum", "consolidation",
                "chest x-ray", "cxr", "chest radiograph",
                "costophrenic", "hilar", "bronchial",
                "air bronchogram", "atelectasis", "effusion",
                "pneumothorax", "nodule", "mass", "infiltrate",
                "reticular", "interstitial", "ap view", "pa view",
                "lateral view", "portable chest"));

        KEYWORDS.put("MRI", Arrays.asList(
                "mri", "magnetic resonance", "magnetic resonance imaging",
                "brain", "lesion", "signal", "contrast",
                "t1", "t2", "flair", "axial", "sagittal", "coronal",
                "weighted", "diffusion", "dw", "adc",
                "white matter", "gray matter", "cortex",
                "ventricle", "cistern", "sulci", "gyri",
                "midline shift", "edema", "enhancement",
                "spin echo", "gradient echo", "fat suppressed"));

        KEYWORDS.put("USG", Arrays.asList(
                "ultrasound", "usg", "sonography", "sonogram",
                "liver", "gallbladder", "spleen", "pancreas",
                "kidney", "renal", "bladder", "prostate",
                "uterus", "ovary", "thyroid", "breast",
                "echo", "abdomen", "pelvis", "foetal", "fetal",
                "doppler", "color flow", "cyst", "mass",
                "parenchyma", "echogenicity", "shadowing",
                "wall thickness", "distal", "proximal",
                "common bile duct", "portal vein", "hepatic"));

        KEYWORDS.put("PRESCRIPTION", Arrays.asList(
                "tablet", "capsule", "mg", "mcg", "dosage",
                "prescribed", "medicine", "medication",
                "once daily", "twice daily", "thrice daily",
                "syrup", "injection", "rx", "prescription",
                "take one", "take two", "before food", "after food",
                "empty stomach", "with water", "oral",
                "intravenous", "intramuscular", "subcutaneous",
                "sos", "prn", "bd", "tds", "od", "qid",
                "ampoule", "vial", "drop", "ointment", "cream"));
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

    public int getScore(String text, String type) {
        if (type.equals("UNKNOWN") || text == null) return 0;
        List<String> words = KEYWORDS.get(type);
        if (words == null) return 0;
        int score = 0;
        for (String word : words) {
            if (text.contains(word)) score++;
        }
        return score;
    }

    public String confidence(String text, String type) {
        int score = getScore(text, type);
        if (score >= 4) return "HIGH";
        if (score >= 2) return "MEDIUM";
        return "LOW";
    }
}
