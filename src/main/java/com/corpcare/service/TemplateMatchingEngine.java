package com.corpcare.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateMatchingEngine {

    private final Map<String, Map<String, Pattern>> vendorPatterns = new LinkedHashMap<>();

    public TemplateMatchingEngine() {
        Map<String, Pattern> shivani = new LinkedHashMap<>();
        shivani.put("employeeName", Pattern.compile(
            "(?:Name|Patient\\s*Name|Patient|PName)\\s*[:.]?\\s*([A-Za-z\\s.]+)", Pattern.CASE_INSENSITIVE));
        shivani.put("age", Pattern.compile(
            "(?:Age)\\s*[:.]?\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE));
        shivani.put("sex", Pattern.compile(
            "(?:Sex|Gender)\\s*[:.]?\\s*(Male|Female|M|F)", Pattern.CASE_INSENSITIVE));
        shivani.put("bloodGroup", Pattern.compile(
            "(?:Blood\\s*Group|Blood\\s*Type|B\\s*Group|Blood\\s*Grp|BG)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)", Pattern.CASE_INSENSITIVE));
        shivani.put("hemoglobin", Pattern.compile(
            "(?:Hemoglobin|Haemoglobin|Hb|HGB)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("rbcCount", Pattern.compile(
            "(?:RBC|RBC\\s*Count|Red\\s*Blood\\s*Cells|Red\\s*Cell\\s*Count)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("wbcCount", Pattern.compile(
            "(?:WBC|WBC\\s*Count|White\\s*Blood\\s*Cells|Total\\s*WBC|White\\s*Cell\\s*Count|TLC|Total\\s*Leucocyte)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("plateletCount", Pattern.compile(
            "(?:Platelet|Platelets|PLT|Platelet\\s*Count|Thrombocyte|PLT\\s*Count)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("esr", Pattern.compile(
            "(?:ESR|Erythrocyte\\s*Sedimentation|Sed\\s*Rate|E\\.?S\\.?R\\.?)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("creatinine", Pattern.compile(
            "(?:Creatinine|Serum\\s*Creatinine|S\\.?\\s*Creatinine|Creat)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("urea", Pattern.compile(
            "(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea|S\\.?\\s*Urea)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("bloodSugar", Pattern.compile(
            "(?:Blood\\s*Sugar|Sugar|FBS|Fasting|Random\\s*Sugar|RBS|Glucose|Blood\\s*Glucose)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("sgpt", Pattern.compile(
            "(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT|Alanine\\s*Transaminase)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("sgot", Pattern.compile(
            "(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT|Aspartate\\s*Transaminase)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("urinePH", Pattern.compile(
            "(?:Urine\\s*pH|Urine\\s*Reaction|pH|U\\.?\\s*pH)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("specificGravity", Pattern.compile(
            "(?:Specific\\s*Gravity|Sp\\.?\\s*Gravity|S\\.?G\\.?|Sp\\s*Grav)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("urineProtein", Pattern.compile(
            "(?:Urine\\s*Protein|Protein\\s*Urine|U\\.?\\s*Protein|Urine\\s*Albumin|Albumin)\\s*[:|]?\\s*(\\d+\\.?\\d*|Nil|Absent|Present|Trace|[A-Za-z+\\-]+)", Pattern.CASE_INSENSITIVE));
        shivani.put("urineGlucose", Pattern.compile(
            "(?:Urine\\s*Glucose|Glucose\\s*Urine|U\\.?\\s*Glucose|Urine\\s*Sugar|Sugar\\s*Urine)\\s*[:|]?\\s*(\\d+\\.?\\d*|Nil|Absent|Present|Trace|[A-Za-z+\\-]+)", Pattern.CASE_INSENSITIVE));
        shivani.put("height", Pattern.compile(
            "(?:Height|Height\\s*cm|Height\\s*in\\s*cm|Ht)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        shivani.put("weight", Pattern.compile(
            "(?:Weight|Weight\\s*kg|Weight\\s*in\\s*kg|Wt)\\s*[:|]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        vendorPatterns.put("SHIVANI_TEMPLATE", shivani);

        Map<String, Pattern> starlab = new LinkedHashMap<>();
        starlab.put("employeeName", Pattern.compile(
            "(?:Name|Patient|Patient\\s*Name|Name\\s*of\\s*Patient)\\s*[:.]?\\s*([A-Za-z\\s.]+)", Pattern.CASE_INSENSITIVE));
        starlab.put("age", Pattern.compile(
            "(?:Age|Age\\s*/\\s*Sex)\\s*[:.]?\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE));
        starlab.put("sex", Pattern.compile(
            "(?:Sex|Gender|Age\\s*/\\s*Sex)\\s*[:.]?\\s*(Male|Female|M|F)", Pattern.CASE_INSENSITIVE));
        starlab.put("bloodGroup", Pattern.compile(
            "(?:Blood\\s*Group|Blood\\s*Type|B\\.?\\s*Group|Blood\\s*Grp)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)", Pattern.CASE_INSENSITIVE));

        Pattern starlabTableLine = Pattern.compile(
            "^\\s*(\\w[\\w\\s/]+?)\\s{2,}(\\d+\\.?\\d*)\\s{2,}([\\d\\.]+\\s*[-–]\\s*[\\d\\.]+|[\\d\\.]+)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        starlab.put("hemoglobin", starlabTableLine);
        starlab.put("rbcCount", starlabTableLine);
        starlab.put("wbcCount", starlabTableLine);
        starlab.put("plateletCount", starlabTableLine);
        starlab.put("esr", starlabTableLine);
        starlab.put("creatinine", Pattern.compile(
            "(?:Creatinine|S\\.?\\s*Creatinine|Serum\\s*Creatinine)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("urea", Pattern.compile(
            "(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea|S\\.?\\s*Urea)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("bloodSugar", Pattern.compile(
            "(?:Blood\\s*Sugar|Sugar|FBS|Fasting|RBS|Random|Glucose|Blood\\s*Glucose)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("sgpt", Pattern.compile(
            "(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("sgot", Pattern.compile(
            "(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("urinePH", Pattern.compile(
            "(?:Urine\\s*pH|pH\\s*\\(Urine\\)|U\\.?\\s*pH|Urine\\s*Reaction)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("specificGravity", Pattern.compile(
            "(?:Specific\\s*Gravity|Sp\\.?\\s*Gr\\.?|S\\.?G\\.?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        starlab.put("urineProtein", Pattern.compile(
            "(?:Protein|Urine\\s*Protein|U\\.?\\s*Protein|Albumin|Urine\\s*Albumin)\\s*[:.]?\\s*(\\d+\\.?\\d*|Nil|Absent|Present|Trace|[A-Za-z+\\-]+)", Pattern.CASE_INSENSITIVE));
        starlab.put("urineGlucose", Pattern.compile(
            "(?:Glucose|Urine\\s*Glucose|U\\.?\\s*Glucose|Sugar\\s*Urine|Urine\\s*Sugar)\\s*[:.]?\\s*(\\d+\\.?\\d*|Nil|Absent|Present|Trace|[A-Za-z+\\-]+)", Pattern.CASE_INSENSITIVE));
        starlab.put("height", Pattern.compile(
            "(?:Height|Height\\s*cm|Ht\\.?)", Pattern.CASE_INSENSITIVE));
        starlab.put("weight", Pattern.compile(
            "(?:Weight|Weight\\s*kg|Wt\\.?)", Pattern.CASE_INSENSITIVE));
        vendorPatterns.put("STARLAB_TEMPLATE", starlab);

        Map<String, Pattern> generic = new LinkedHashMap<>();
        generic.put("employeeName", Pattern.compile(
            "(?:Name|Patient\\s*Name|Patient)\\s*[:.]?\\s*([A-Za-z\\s.]+)", Pattern.CASE_INSENSITIVE));
        generic.put("age", Pattern.compile(
            "(?:Age)\\s*[:.]?\\s*(\\d{1,3})", Pattern.CASE_INSENSITIVE));
        generic.put("sex", Pattern.compile(
            "(?:Sex|Gender)\\s*[:.]?\\s*(Male|Female|M|F)", Pattern.CASE_INSENSITIVE));
        generic.put("bloodGroup", Pattern.compile(
            "(?:Blood\\s*Group|Blood\\s*Type|B\\s*Group)\\s*[:.]?\\s*([A-Za-z+\\-\\d]+)", Pattern.CASE_INSENSITIVE));
        generic.put("hemoglobin", Pattern.compile(
            "(?:Hemoglobin|Haemoglobin|Hb|HGB)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("rbcCount", Pattern.compile(
            "(?:RBC|RBC\\s*Count|Red\\s*Blood\\s*Cells)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("wbcCount", Pattern.compile(
            "(?:WBC|WBC\\s*Count|White\\s*Blood\\s*Cells|Total\\s*WBC|TLC)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("plateletCount", Pattern.compile(
            "(?:Platelet|Platelets|PLT|Platelet\\s*Count)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("esr", Pattern.compile(
            "(?:ESR|Erythrocyte\\s*Sedimentation|Sed\\s*Rate)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("creatinine", Pattern.compile(
            "(?:Creatinine|Serum\\s*Creatinine|S\\.?\\s*Creatinine)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("urea", Pattern.compile(
            "(?:Urea|Blood\\s*Urea|BUN|Serum\\s*Urea)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("bloodSugar", Pattern.compile(
            "(?:Blood\\s*Sugar|Sugar|FBS|Fasting|RBS|Random|Glucose|Blood\\s*Glucose)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("sgpt", Pattern.compile(
            "(?:SGPT|ALT|Alanine\\s*Aminotransferase|GPT)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("sgot", Pattern.compile(
            "(?:SGOT|AST|Aspartate\\s*Aminotransferase|GOT)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("urinePH", Pattern.compile(
            "(?:Urine\\s*pH|pH|U\\.?\\s*pH)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("specificGravity", Pattern.compile(
            "(?:Specific\\s*Gravity|Sp\\.?\\s*Gravity|S\\.?G\\.?)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("urineProtein", Pattern.compile(
            "(?:Protein|Urine\\s*Protein|Albumin)\\s*[:.]?\\s*(\\d+\\.?\\d*|Nil|Absent|Present|Trace|NEGATIVE|POSITIVE|[A-Za-z+\\-]+)", Pattern.CASE_INSENSITIVE));
        generic.put("urineGlucose", Pattern.compile(
            "(?:Glucose|Urine\\s*Glucose|Sugar\\s*Urine)\\s*[:.]?\\s*(\\d+\\.?\\d*|Nil|Absent|Present|Trace|NEGATIVE|POSITIVE|[A-Za-z+\\-]+)", Pattern.CASE_INSENSITIVE));
        generic.put("height", Pattern.compile(
            "(?:Height|Height\\s*cm|Height\\s*in\\s*cm|Ht)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        generic.put("weight", Pattern.compile(
            "(?:Weight|Weight\\s*kg|Weight\\s*in\\s*kg|Wt)\\s*[:.]?\\s*(\\d+\\.?\\d*)", Pattern.CASE_INSENSITIVE));
        vendorPatterns.put("GENERIC_TEMPLATE", generic);
    }

    public Map<String, String> extract(String text, String vendorFormat) {
        Map<String, String> extracted = new LinkedHashMap<>();
        Map<String, Pattern> patterns = vendorPatterns.getOrDefault(vendorFormat, vendorPatterns.get("GENERIC_TEMPLATE"));

        String[] keys = {"employeeName", "age", "sex", "bloodGroup", "hemoglobin", "rbcCount",
            "wbcCount", "plateletCount", "esr", "creatinine", "urea", "bloodSugar",
            "sgpt", "sgot", "urinePH", "specificGravity", "urineProtein", "urineGlucose",
            "height", "weight"};

        for (String key : keys) {
            Pattern p = patterns.get(key);
            if (p != null) {
                String value = matchPattern(text, p);
                extracted.put(key, value != null ? value : "Not Available");
            } else {
                extracted.put(key, "Not Available");
            }
        }

        return extracted;
    }

    private String matchPattern(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            if (m.groupCount() >= 1) {
                return m.group(1).trim();
            }
            return m.group(0).trim();
        }
        return null;
    }
}
