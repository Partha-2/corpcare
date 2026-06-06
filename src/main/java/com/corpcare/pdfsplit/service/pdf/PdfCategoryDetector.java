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
                "triglyceride", "hba1c", "thyroid", "tsh",
                "complete blood count", "differential",
                "hematocrit", "mcv", "mch", "mchc",
                "rdw", "neutrophil", "lymphocyte", "monocyte",
                "eosinophil", "basophil", "esr", "crp",
                "hdl", "ldl", "vldl", "bilirubin",
                "alkaline phosphatase", "alt", "ast", "ggt",
                "sodium", "potassium", "chloride", "calcium",
                "uric acid", "iron", "ferritin", "vitamin b12",
                "vitamin d", "folic acid", "insulin", "prolactin"));

        KEYWORDS.put("lab_urine", Arrays.asList(
                "urine", "urinalysis", "urea",
                "creatinine", "kidney", "renal",
                "albumin", "nitrite", "leucocyte",
                "specific gravity", "ph", "protein",
                "ketone", "bilirubin", "urobilinogen",
                "pus cells", "epithelial cells", "casts",
                "crystals", "bacteria", "mucus",
                "microalbumin", "creatinine clearance",
                "bun", "glomerular", "nephrotic",
                "urine routine", "urine analysis",
                "spot urine", "24 hour urine"));

        KEYWORDS.put("eye", Arrays.asList(
                "eye", "vision", "ophthalmology",
                "retina", "cornea", "visual acuity",
                "cataract", "glaucoma", "ocular",
                "intraocular pressure", "iop",
                "fundus", "optic disc", "macula",
                "fovea", "lens", "iris", "pupil",
                "conjunctiva", "sclera", "eyelid",
                "refraction", "sphere", "cylinder", "axis",
                "visual field", "perimetry", "tonometry",
                "slit lamp", "dilated", "retinopathy",
                "macular edema", "dry eye", "floaters",
                "spectacle", "contact lens", "lasik",
                "snellen", "near vision", "distance vision"));

        KEYWORDS.put("chest", Arrays.asList(
                "chest", "lung", "pulmonary",
                "xray", "x-ray", "cardiac", "heart",
                "radiograph", "thorax", "ecg",
                "pneumonia", "bronchitis", "asthma",
                "copd", "emphysema", "tuberculosis",
                "pleural effusion", "pneumothorax",
                "respiratory rate", "oxygen saturation",
                "spo2", "pft", "spirometry",
                "fev1", "fvc", "pef", "dlco",
                "echocardiogram", "echo", "ejection fraction",
                "troponin", "ck-mb", "bnp", "nt-probnp",
                "coronary", "myocardial", "infarction",
                "cardiac enzyme", "lipid profile",
                "stress test", "tmt", "angiogram",
                "aorta", "pulmonary artery", "ventricle",
                "aortic", "mitral", "tricuspid", "pulmonic",
                "st elevation", "st depression", "t inversion",
                "cardiomegaly", "calcification", "fibrosis"));
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

    public int getScore(String text, String category) {
        if (text == null || category == null) return 0;
        List<String> words = KEYWORDS.get(category);
        if (words == null) return 0;
        int score = 0;
        for (String word : words) {
            if (text.contains(word)) score++;
        }
        return score;
    }

    public String confidence(String text, String category) {
        int score = getScore(text, category);
        if (score >= 4) return "HIGH";
        if (score >= 2) return "MEDIUM";
        return "LOW";
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
