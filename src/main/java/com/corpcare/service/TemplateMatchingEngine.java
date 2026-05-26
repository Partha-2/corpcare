package com.corpcare.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateMatchingEngine {

    private static final String PAREN = "(?:\\s*\\([^)]*\\))?";
    private static final String VAL = "(\\d[\\d,.]*)";
    private static final String QUAL = "(\\d+(?:\\.\\d+)?|Nil|Absent|Present|Trace|NEGATIVE|POSITIVE|Negative|Positive|normal|abnormal|[A-Za-z]+)";

    private final Map<String, Map<String, Pattern>> vendorPatterns = new LinkedHashMap<>();

    public TemplateMatchingEngine() {
        vendorPatterns.put("SHIVANI_TEMPLATE", buildShivani());
        vendorPatterns.put("STARLAB_TEMPLATE", buildStarlab());
        vendorPatterns.put("GENERIC_TEMPLATE", buildGeneric());
    }

    private Map<String, Pattern> buildShivani() {
        Map<String, Pattern> m = new LinkedHashMap<>();
        String sep = PAREN + "\\s*[:|]?\\s*";
        m.put("employeeName", p("(?:Name|Patient\\s*Name|Patient|PName)\\s*[:.]?\\s*(?!Ref\\.)([A-Za-z\\s.]+)"));
        m.put("age", p("(?:Age)\\s*[:.]?\\s*(\\d{1,3})\\b"));
        m.put("sex", p("(?:Sex|Gender)\\s*[:.]?\\s*(Male|Female|M|F)"));
        m.put("bloodGroup", p("(?:Blood\\s*Group|Blood\\s*Type|B\\s*Group|Blood\\s*Grp|BG)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)"));
        m.put("hemoglobin", p("(?:Haemoglobin|Hemoglobin|Hb|HGB)" + sep + VAL));
        m.put("rbcCount", p("(?:RBC|RBC\\s*Count|Red\\s*Blood\\s*Cells?)" + sep + VAL));
        m.put("wbcCount", p("(?:WBC|WBC\\s*Count|White\\s*Blood\\s*Cells?|Total\\s*WBC|TLC|Total\\s*Leucocyte)" + sep + VAL));
        m.put("plateletCount", p("(?:Platelet|Platelets|PLT|Platelet\\s*Count|Thrombocyte)" + sep + VAL));
        m.put("esr", p("(?:ESR|Erythrocyte\\s*Sedimentation|Sed\\s*Rate)" + sep + VAL));
        m.put("creatinine", p("(?:Creatinine|Serum\\s*Creatinine|S\\.?\\s*Creatinine|Creat)" + sep + VAL));
        m.put("urea", p("(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea)" + sep + VAL));
        m.put("bloodSugar", p("(?:Blood\\s*Sugar|Sugar|FBS|Fasting|RBS|Random|Glucose|Blood\\s*Glucose|PPBS|Post\\s*Prandial)" + sep + VAL));
        m.put("sgpt", p("(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT)" + sep + VAL));
        m.put("sgot", p("(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT)" + sep + VAL));
        m.put("urinePH", p("(?:Urine\\s*pH|pH|U\\.?\\s*pH|Urine\\s*Reaction)" + sep + VAL));
        m.put("specificGravity", p("(?:Specific\\s*Gravity|Sp\\.?\\s*Gr\\.?|S\\.?G\\.?)" + sep + VAL));
        m.put("urineProtein", p("(?:Protein|Urine\\s*Protein|U\\.?\\s*Protein|Albumin)" + sep + QUAL + "(?:\\s|$)"));
        m.put("urineGlucose", p("(?:Glucose|(?<!Blood )Sugar|Urine\\s*Glucose|Urine\\s*Sugar)" + sep + QUAL + "(?:\\s|$)"));
        m.put("height", p("(?:Height|Ht)" + sep + VAL));
        m.put("weight", p("(?:Weight|Wt)" + sep + VAL));
        return m;
    }

    private Map<String, Pattern> buildStarlab() {
        Map<String, Pattern> m = new LinkedHashMap<>();
        String sep = PAREN + "\\s*[:.]?\\s*";
        m.put("employeeName", p("(?:Name|Patient|Patient\\s*Name|Name\\s*of\\s*Patient)\\s*[:.]?\\s*(?!Ref\\.)([A-Za-z\\s.]+)"));
        m.put("age", p("(?:Age|Age\\s*/\\s*Sex)\\s*[:.]?\\s*(\\d{1,3})"));
        m.put("sex", p("(?:Sex|Gender|Age\\s*/\\s*Sex)\\s*[:.]?\\s*(Male|Female|M|F)"));
        m.put("bloodGroup", p("(?:Blood\\s*Group|Blood\\s*Type|B\\.?\\s*Group|Blood\\s*Grp)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)"));
        String tblSep = "(?:\\s*\\([^)]*\\))?" + "[ \\t]{2,}";
        m.put("hemoglobin", p("(?:Haemoglobin|Hemoglobin|Hb|HGB)" + tblSep + VAL));
        m.put("rbcCount", p("(?:RBC\\s*Count|Red\\s*Blood\\s*Cells?|RBC)" + tblSep + VAL));
        m.put("wbcCount", p("(?:Total\\s*WBC|WBC\\s*Count|White\\s*Blood\\s*Cells?|TLC)" + tblSep + VAL));
        m.put("plateletCount", p("(?:Platelet\\s*Count|Platelets|PLT)" + tblSep + VAL));
        m.put("esr", p("(?:ESR|Erythrocyte\\s*Sedimentation|Sed\\s*Rate)" + tblSep + VAL));
        m.put("creatinine", p("(?:Creatinine|S\\.?\\s*Creatinine|Serum\\s*Creatinine)" + sep + VAL));
        m.put("urea", p("(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea)" + sep + VAL));
        m.put("bloodSugar", p("(?:Blood\\s*Sugar|Sugar|FBS|Fasting|RBS|Random|Glucose|Blood\\s*Glucose)" + sep + VAL));
        m.put("sgpt", p("(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT)" + sep + VAL));
        m.put("sgot", p("(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT)" + sep + VAL));
        m.put("urinePH", p("(?:Urine\\s*pH|pH|U\\.?\\s*pH|Urine\\s*Reaction)" + sep + VAL));
        m.put("specificGravity", p("(?:Specific\\s*Gravity|Sp\\.?\\s*Gr\\.?|S\\.?G\\.?)" + sep + VAL));
        m.put("urineProtein", p("(?:Protein|Urine\\s*Protein|U\\.?\\s*Protein|Albumin)" + sep + QUAL + "(?:\\s|$)"));
        m.put("urineGlucose", p("(?:Glucose|(?<!Blood )Sugar|Urine\\s*Glucose|Urine\\s*Sugar)" + sep + QUAL + "(?:\\s|$)"));
        m.put("height", p("(?:Height|Ht)" + sep + VAL));
        m.put("weight", p("(?:Weight|Wt)" + sep + VAL));
        return m;
    }

    private Map<String, Pattern> buildGeneric() {
        Map<String, Pattern> m = new LinkedHashMap<>();
        String sep = PAREN + "\\s*[:.]?\\s*";
        m.put("employeeName", p("(?:Name|Patient\\s*Name|Patient)\\s*[:.]?\\s*(?!Ref\\.)([A-Za-z\\s.]+)"));
        m.put("age", p("(?:Age)\\s*[:.]?\\s*(\\d{1,3})"));
        m.put("sex", p("(?:Sex|Gender)\\s*[:.]?\\s*(Male|Female|M|F)"));
        m.put("bloodGroup", p("(?:Blood\\s*Group|Blood\\s*Type|B\\s*Group)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)"));
        m.put("hemoglobin", p("(?:Haemoglobin|Hemoglobin|Hb|HGB)" + sep + VAL));
        m.put("rbcCount", p("(?:RBC|RBC\\s*Count|Red\\s*Blood\\s*Cells?)" + sep + VAL));
        m.put("wbcCount", p("(?:WBC|WBC\\s*Count|White\\s*Blood\\s*Cells?|Total\\s*WBC|TLC)" + sep + VAL));
        m.put("plateletCount", p("(?:Platelet|Platelets|PLT|Platelet\\s*Count)" + sep + VAL));
        m.put("esr", p("(?:ESR|Erythrocyte\\s*Sedimentation|Sed\\s*Rate)" + sep + VAL));
        m.put("creatinine", p("(?:Creatinine|Serum\\s*Creatinine|S\\.?\\s*Creatinine)" + sep + VAL));
        m.put("urea", p("(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea)" + sep + VAL));
        m.put("bloodSugar", p("(?:Blood\\s*Sugar|Sugar|FBS|Fasting|RBS|Random|Glucose|Blood\\s*Glucose)" + sep + VAL));
        m.put("sgpt", p("(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT)" + sep + VAL));
        m.put("sgot", p("(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT)" + sep + VAL));
        m.put("urinePH", p("(?:Urine\\s*pH|pH|U\\.?\\s*pH|Urine\\s*Reaction)" + sep + VAL));
        m.put("specificGravity", p("(?:Specific\\s*Gravity|Sp\\.?\\s*Gr\\.?|S\\.?G\\.?)" + sep + VAL));
        m.put("urineProtein", p("(?:Protein|Urine\\s*Protein|Albumin)" + sep + QUAL + "(?:\\s|$)"));
        m.put("urineGlucose", p("(?:Glucose|(?<!Blood )Sugar|Urine\\s*Glucose|Urine\\s*Sugar)" + sep + QUAL + "(?:\\s|$)"));
        m.put("height", p("(?:Height|Ht)" + sep + VAL));
        m.put("weight", p("(?:Weight|Wt)" + sep + VAL));
        return m;
    }

    public Map<String, String> extract(String text, String vendorFormat) {
        Map<String, String> extracted = new LinkedHashMap<>();
        Map<String, Pattern> patterns = vendorPatterns.getOrDefault(vendorFormat, vendorPatterns.get("GENERIC_TEMPLATE"));
        String[] keys = {"employeeName","age","sex","bloodGroup","hemoglobin","rbcCount",
            "wbcCount","plateletCount","esr","creatinine","urea","bloodSugar",
            "sgpt","sgot","urinePH","specificGravity","urineProtein","urineGlucose","height","weight"};
        for (String key : keys) {
            Pattern p = patterns.get(key);
            if (p != null) {
                String value = matchPattern(text, p);
                extracted.put(key, value != null ? cleanValue(value) : "Not Available");
            } else {
                extracted.put(key, "Not Available");
            }
        }
        return extracted;
    }

    private String matchPattern(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            return m.groupCount() >= 1 ? m.group(1).trim() : m.group(0).trim();
        }
        return null;
    }

    private String cleanValue(String v) {
        if (v == null || v.isBlank()) return v;
        String cleaned = v.replaceAll("[^\\d.,A-Za-z+\\-].*$", "").trim();
        return cleaned.isBlank() ? v.split("\\s+")[0] : cleaned;
    }

    private Pattern p(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    }
}
