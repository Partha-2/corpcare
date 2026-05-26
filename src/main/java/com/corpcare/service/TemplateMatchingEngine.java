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
    private static final String QUAL = "(\\d+(?:\\.\\d+)?|Nil|Absent|Present|Trace|NEGATIVE|POSITIVE|Negative|Positive|Normal|Abnormal|Non-Reactive)";

    private final Map<String, Map<String, Pattern>> vendorPatterns = new LinkedHashMap<>();

    private static final String NUM = "\\d+(?:[.,]\\d+)?";
    private static final String RANGE = NUM + "\\s*[-–]\\s*" + NUM + "[^\\d.]*?";

    public TemplateMatchingEngine() {
        vendorPatterns.put("SHIVANI_TEMPLATE", buildShivani());
        vendorPatterns.put("STARLAB_TEMPLATE", buildStarlab());
        vendorPatterns.put("GENERIC_TEMPLATE", buildGeneric());
    }

    private Map<String, Pattern> buildShivani() {
        Map<String, Pattern> m = new LinkedHashMap<>();
        String sep = PAREN + "\\s*[:|]\\s*";
        m.put("employeeName", p("(?:Name|Patient\\s*Name|Patient|PName|MR\\.?)\\s*[:.]?\\s*(?!Ref\\.)([A-Za-z\\s.]+)"));
        m.put("age", p("(?:Age|Age/Sex)\\s*[:.]?\\s*(\\d{1,3})\\b"));
        m.put("sex", p("(?:Sex|Gender|Age/Sex)\\s*[:.]?\\s*(Male|Female|M|F)"));
        m.put("bloodGroup", p("(?:Blood\\s*Group|Blood\\s*Type|B\\s*Group|Blood\\s*Grp|BG|ABO)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)"));
        m.put("hemoglobin", p("(?:Haemoglobin|Hemoglobin|Hb|HGB)" + sep + VAL));
        m.put("rbcCount", p("(?:RBC|RBC\\s*Count|Red\\s*Blood\\s*Cells?)" + sep + VAL));
        m.put("wbcCount", p("(?:WBC|WBC\\s*Count|White\\s*Blood\\s*Cells?|Total\\s*WBC|TLC|Total\\s*Leucocyte)" + sep + VAL));
        m.put("plateletCount", p("(?:Platelet|Platelets|PLT|Platelet\\s*Count|Thrombocyte)" + sep + VAL));
        m.put("esr", p("(?:ESR|Erythrocyte\\s*Sedimentation|Sed\\s*Rate|Corrected\\s*ESR)" + sep + VAL));
        m.put("creatinine", p("(?:Creatinine|Serum\\s*Creatinine|S\\.?\\s*Creatinine|Creat)" + sep + VAL));
        m.put("urea", p("(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea)" + sep + VAL));
        m.put("bloodSugar", p("(?:Blood\\s*Sugar|Sugar|FBS|Fasting|RBS|Random|Glucose|Blood\\s*Glucose|PPBS|Post\\s*Prandial)" + sep + VAL));
        m.put("sgpt", p("(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT)" + sep + VAL));
        m.put("sgot", p("(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT)" + sep + VAL));
        m.put("urinePH", p("(?:Urine\\s*pH|pH|U\\.?\\s*pH|Urine\\s*Reaction)" + sep + VAL));
        m.put("specificGravity", p("(?:Specific\\s*Gravity|Sp\\.?\\s*Gr\\.?|S\\.?G\\.?)" + sep + VAL));
        m.put("urineProtein", p("(?:Protein|Urine\\s*Protein|U\\.?\\s*Protein|Albumin)" + sep + QUAL + "(?:\\s|$)"));
        m.put("urineGlucose", p("(?:Glucose|(?<!Blood )Sugar|Urine\\s*Glucose|Urine\\s*Sugar)" + sep + QUAL + "(?:\\s|$)"));
        m.put("pcv", p("(?:PCV|Packed\\s*Cell\\s*Volume|HCT|Haematocrit|Hematocrit)" + sep + VAL));
        m.put("mcv", p("(?:MCV|Mean\\s*Corpuscular\\s*Volume)" + sep + VAL));
        return m;
    }

    private Map<String, Pattern> buildStarlab() {
        Map<String, Pattern> m = new LinkedHashMap<>();
        String sep = PAREN + "\\s*[:.]?\\s*";
        m.put("employeeName", p("(?:Name|Patient|Patient\\s*Name|Name\\s*of\\s*Patient|MR\\.?)\\s*[:.]?\\s*(?!Ref\\.|SELF)([A-Za-z\\s.]+)"));
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
        m.put("pcv", p("(?:PCV|Packed\\s*Cell\\s*Volume|HCT|Haematocrit|Hematocrit)" + tblSep + VAL));
        m.put("mcv", p("(?:MCV|Mean\\s*Corpuscular\\s*Volume)" + tblSep + VAL));
        return m;
    }

    private Map<String, Pattern> buildGeneric() {
        Map<String, Pattern> m = new LinkedHashMap<>();
        String sep = PAREN + "\\s*[:.]?\\s*";
        m.put("employeeName", p("(?:Name|Patient\\s*Name|Patient|MR\\.?)\\s*[:.]?\\s*(?!Ref\\.|SELF)([A-Za-z\\s.]+)"));
        m.put("age", p("(?:Age|Age/Sex|Age\\\\s*Years)\\s*[:./]?\\s*(\\d{1,3})"));
        m.put("sex", p("(?:Sex|Gender|Age/Sex|\\d+\\s*Years\\s*/\\s*)(Male|Female|M|F)"));
        m.put("bloodGroup", p("(?:Blood\\s*Group|Blood\\s*Type|B\\s*Group|ABO)\\s*[:.]?\\s*([A-Za-z+\\-\\d\"]+)"));
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
        m.put("pcv", p("(?:PCV|Packed\\s*Cell\\s*Volume|HCT|Haematocrit|Hematocrit)" + sep + VAL));
        m.put("mcv", p("(?:MCV|Mean\\s*Corpuscular\\s*Volume)" + sep + VAL));
        return m;
    }

    private static final Map<String, String[]> KEY_PARAM_NAMES = new LinkedHashMap<>();
    static {
        KEY_PARAM_NAMES.put("employeeName", new String[]{"Name", "Patient", "MR\\.", "Patient\\s*Name"});
        KEY_PARAM_NAMES.put("age", new String[]{"Age", "Age/Sex"});
        KEY_PARAM_NAMES.put("sex", new String[]{"Male", "Female", "M$", "F$"});
        KEY_PARAM_NAMES.put("bloodGroup", new String[]{"Blood\\s*Group", "ABO", "Blood\\s*Type"});
        KEY_PARAM_NAMES.put("hemoglobin", new String[]{"Haemoglobin", "Hemoglobin", "Hb", "HGB"});
        KEY_PARAM_NAMES.put("rbcCount", new String[]{"RBC", "RBCs", "Red\\s*Blood\\s*Cells?"});
        KEY_PARAM_NAMES.put("wbcCount", new String[]{"Total\\s*WBCs?", "WBC", "White\\s*Blood\\s*Cells?"});
        KEY_PARAM_NAMES.put("plateletCount", new String[]{"Platelets?", "PLT", "Platelet\\s*Count"});
        KEY_PARAM_NAMES.put("esr", new String[]{"ESR", "Erythrocyte\\s*Sedimentation", "Corrected\\s*ESR"});
        KEY_PARAM_NAMES.put("creatinine", new String[]{"Creatinine", "Serum\\s*Creatinine"});
        KEY_PARAM_NAMES.put("urea", new String[]{"Urea", "Blood\\s*Urea", "BUN", "Serum\\s*Urea"});
        KEY_PARAM_NAMES.put("bloodSugar", new String[]{"Blood\\s*Sugar", "RBS", "Random\\s*Blood\\s*Sugar", "FBS", "Fasting", "Random\\s*Sugar"});
        KEY_PARAM_NAMES.put("sgpt", new String[]{"SGPT", "ALT"});
        KEY_PARAM_NAMES.put("sgot", new String[]{"SGOT", "AST"});
        KEY_PARAM_NAMES.put("urinePH", new String[]{"pH", "PH"});
        KEY_PARAM_NAMES.put("specificGravity", new String[]{"Specific\\s*Gravity", "Sp\\.?\\s*Gr\\.?"});
        KEY_PARAM_NAMES.put("urineProtein", new String[]{"Protein", "Urine\\s*Protein"});
        KEY_PARAM_NAMES.put("urineGlucose", new String[]{"Glucose", "(?<!Blood )Sugar", "Urine\\s*Sugar"});
        KEY_PARAM_NAMES.put("pcv", new String[]{"PCV", "Packed\\s*Cell\\s*Volume", "HCT", "Haematocrit"});
        KEY_PARAM_NAMES.put("mcv", new String[]{"MCV", "Mean\\s*Corpuscular\\s*Volume"});
    }

    public Map<String, String> extract(String text, String vendorFormat) {
        Map<String, String> extracted = new LinkedHashMap<>();
        Map<String, Pattern> patterns = vendorPatterns.getOrDefault(vendorFormat, vendorPatterns.get("GENERIC_TEMPLATE"));

        String[] keys = {"employeeName","age","sex","bloodGroup","hemoglobin","rbcCount",
            "wbcCount","plateletCount","esr","creatinine","urea","bloodSugar",
            "sgpt","sgot","urinePH","specificGravity","urineProtein","urineGlucose","pcv","mcv"};

        String[] lines = text.split("\\r?\\n");

        for (String key : keys) {
            String value = null;
            Pattern p = patterns.get(key);
            if (p != null) {
                value = matchPattern(text, p);
            }
            if (value == null) {
                value = tryFallbackPatterns(text, lines, key);
            }
            if (value == null) {
                value = tryNextLinePattern(lines, key);
            }
            extracted.put(key, value != null ? cleanValue(value) : "Not Available");
        }
        return extracted;
    }

    private String tryFallbackPatterns(String text, String[] lines, String key) {
        String[] paramNames = KEY_PARAM_NAMES.get(key);
        if (paramNames == null) return null;
        String joined = "(?:" + String.join("|", paramNames) + ")";
        boolean isQual = key.equals("urineProtein") || key.equals("urineGlucose");
        boolean isNumeric = !isQual;

        // Special patterns for age/sex
        if (key.equals("age")) {
            Pattern p = Pattern.compile("(\\d{1,3})\\s*Years?", Pattern.CASE_INSENSITIVE);
            String v = matchPattern(text, p);
            if (v != null) return v;
        }
        if (key.equals("sex")) {
            Pattern p = Pattern.compile("[/\\\\s]+(M|F)(?:ale|emale)?(?:\\s|$)", Pattern.CASE_INSENSITIVE);
            String v = matchPattern(text, p);
            if (v != null) return v;
        }
        if (key.equals("bloodGroup")) {
            Pattern p = Pattern.compile("[\"\"'']\\s*([A-Z][A-Za-z0-9+-]+)\\s*[\"\"'']", Pattern.CASE_INSENSITIVE);
            String v = matchPattern(text, p);
            if (v != null) return v;
            // Also try after "BLOOD GROUP" keyword
            p = Pattern.compile("(?:Blood\\s*Group|ABO).*?[\"\"'']?\\s*([A-Z][A-Za-z0-9+-]+)\\s*[\"\"'']?", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            v = matchPattern(text, p);
            if (v != null) return v;
        }

        String valPattern = isQual ? "(Absent|Nil|Negative|Normal|Present|Positive|Non-Reactive|Trace)" : "(\\d+(?:[.,]\\d+)?)";

        // Pattern 1: RANGE then VALUE then PARAM_NAME (Star Lab reversed format, e.g. "13.5 - 17.5 g/dL14.6Haemoglobin")
        Pattern rangeVal = Pattern.compile(RANGE + valPattern + "[ \\t]*" + joined + "\\b", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        String v = matchPattern(text, rangeVal);
        if (v != null) return v;

        // Pattern 2: VALUE before PARAM_NAME (generic reversed, e.g. "14.6Haemoglobin")
        Pattern rev = Pattern.compile(valPattern + "[ \\t]*" + joined + "\\b", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        v = matchPattern(text, rev);
        if (v != null) return v;

        // Pattern 3: PARAM_NAME at start of line, value at end (Shivani graphic format)
        Pattern end = Pattern.compile("(?m)^\\s*" + joined + "[^\\d]*" + RANGE + valPattern + "\\s*$", Pattern.CASE_INSENSITIVE);
        v = matchPattern(text, end);
        if (v != null) return v;

        // Pattern 4: First number after param name on line (space-separated format)
        Pattern lastOnLine = Pattern.compile("(?m)^\\s*" + joined + ".*?(\\d+(?:[.,]\\d+)?)\\b", Pattern.CASE_INSENSITIVE);
        v = matchPattern(text, lastOnLine);
        if (v != null) return v;

        return null;
    }

    private String tryNextLinePattern(String[] lines, String key) {
        String[] paramNames = KEY_PARAM_NAMES.get(key);
        if (paramNames == null) return null;
        String joined = "(?:" + String.join("|", paramNames) + ")";
        boolean isQual = key.equals("urineProtein") || key.equals("urineGlucose");

        for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i].trim();
            if (Pattern.compile(joined, Pattern.CASE_INSENSITIVE).matcher(line).find()) {
                for (int j = i + 1; j < Math.min(i + 4, lines.length); j++) {
                    String next = lines[j].trim();
                    if (next.isEmpty()) continue;
                    if (Pattern.compile("(?i)(method|enzymatic|westergen|westergren|reference|interpretation)").matcher(next).find()) continue;
                    if (isQual) {
                        // For qual keys, check if next line has a qual word
                        Matcher qm = Pattern.compile("(Nil|Absent|Negative|Normal|Present|Positive|Non-Reactive)\\s*$", Pattern.CASE_INSENSITIVE).matcher(next);
                        if (qm.find()) return qm.group(1);
                    }
                    Matcher m = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*$").matcher(next);
                    if (m.find() && !next.matches(".*\\b(?:Glucose|Sugar|Protein|Creatinine|Urea|WBC|RBC|ESR|Hb|pH|SG|Blood)\\b.*")) {
                        return m.group(1);
                    }
                    break;
                }
                break;
            }
        }
        return null;
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
