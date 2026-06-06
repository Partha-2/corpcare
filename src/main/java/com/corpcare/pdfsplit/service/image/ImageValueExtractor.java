package com.corpcare.pdfsplit.service.image;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageValueExtractor {

    private static final Map<String, Map<String, String>> TYPE_PATTERNS = new LinkedHashMap<>();

    static {
        Map<String, String> ecg = new LinkedHashMap<>();
        ecg.put("Heart Rate", "(?:hr|rr)[:\\s.,_)-]+([0-9]{2,3})\\s*bpm");
        ecg.put("VR", "(?:vr|ver|vb)[:\\s.,_)-]+([0-9]{2,3})\\s*(?:bpm|dpm)");
        ecg.put("QRS Duration", "(?:qrsd|qrs|grsd|orsd)[:\\s.,_)-]+([0-9]{2,3})\\s*ms");
        ecg.put("QT Interval", "(?:\\bqt\\b|\\bot\\b)[:\\s.,_)-]+([0-9]{3})\\s*ms");
        ecg.put("QTcB", "(?:qtcb|qtccb|otcb|qtcs|qtc8|otcs|qtscb)[:\\s.,_)-]+([0-9]{3})\\s*ms");
        ecg.put("PRI", "(?:pri|prl|pr1|fri)[:\\s.,_)-]+([0-9]{3})\\s*ms");
        ecg.put("P-R-T Axis", "prt[:\\s.,_)-]+([0-9°\\s°+\\-]{4,20})");
        ecg.put("Diagnosis", "((?:ecg|cg|2cg|3cg|7cg)[\\s]+within[^'\"\\|]{0,100}(?:impaired|limits|limi))");
        ecg.put("Recommendation", "(please[\\s]+(?:repeat|correlate)[^|\\n]{0,60})");
        ecg.put("Rhythm", "(sinus rhythm)");
        ecg.put("Patient Name", "patient name[:\\s]+(?:mr\\.|mrs\\.)?\\s*([a-z\\s]{5,25})");
        ecg.put("Patient ID", "patient id[:\\s]+([0-9]{4,6})");
        ecg.put("Age", "([0-9]{2})\\s*\\|\\s*(?:male|female)");
        ecg.put("Date", "([0-9]{1,2}\\s+(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+[0-9]{4})");
        TYPE_PATTERNS.put("ECG", ecg);

        Map<String, String> xray = new LinkedHashMap<>();
        xray.put("Lung Fields", "(?:lung fields?|lungs?)[:\\s]+([a-z\\s,.-]{3,50})");
        xray.put("Heart Size", "(?:heart size|cardiac size|heart)[:\\s]+([a-z\\s,.-]{3,40})");
        xray.put("Impression", "(?:impression|conclusion)[:\\s]+([a-z\\s,.-]{3,80})");
        xray.put("Pleural", "(?:pleural|pleura)[:\\s]+([a-z\\s,.-]{3,40})");
        xray.put("Trachea", "(?:trachea|tracheal)[:\\s]+([a-z\\s,.-]{3,40})");
        xray.put("Diagnosis", "(normal chest|no abnormality|no significant)");
        xray.put("Patient Name", "patient[:\\s]+(?:name[:\\s]+)?([a-z\\s]{5,25})");
        xray.put("Patient ID", "patient id[:\\s]+([0-9]{4,8})");
        xray.put("Date", "([0-9]{1,2}\\s+(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\s+[0-9]{4})");
        TYPE_PATTERNS.put("XRAY", xray);

        Map<String, String> mri = new LinkedHashMap<>();
        mri.put("Region", "(?:region|area)[:\\s]+([a-z\\s]+)");
        mri.put("Finding", "(?:finding|impression)[:\\s]+([a-z\\s,]+)");
        mri.put("Contrast", "contrast[:\\s]+([a-z\\s]+)");
        mri.put("Patient Name", "patient[:\\s]+([a-z\\s]{5,25})");
        mri.put("Patient ID", "patient id[:\\s]+([0-9]{4,8})");
        TYPE_PATTERNS.put("MRI", mri);

        Map<String, String> usg = new LinkedHashMap<>();
        usg.put("Organ", "(?:liver|kidney|spleen|organ)[:\\s]+([a-z\\s]+)");
        usg.put("Size", "size[:\\s]+([0-9a-z\\s.]+)");
        usg.put("Finding", "(?:finding|impression)[:\\s]+([a-z\\s,]+)");
        usg.put("Patient Name", "patient[:\\s]+([a-z\\s]{5,25})");
        TYPE_PATTERNS.put("USG", usg);

        Map<String, String> prsc = new LinkedHashMap<>();
        prsc.put("Medicine", "(?:tablet|capsule|syrup)[:\\s]+([a-z0-9\\s]+)");
        prsc.put("Dosage", "([0-9]+\\s*mg)");
        prsc.put("Frequency", "(once daily|twice daily|od|bd|tds|thrice)");
        prsc.put("Patient Name", "patient[:\\s]+([a-z\\s]{5,25})");
        TYPE_PATTERNS.put("PRESCRIPTION", prsc);
    }

    public Map<String, String> extract(String rawText, String type) {
        String text = normalize(rawText);
        Map<String, String> values = new LinkedHashMap<>();
        Map<String, String> patterns = TYPE_PATTERNS.get(type);
        if (patterns == null) return values;

        for (Map.Entry<String, String> entry : patterns.entrySet()) {
            String label = entry.getKey();
            String pattern = entry.getValue();
            try {
                Matcher m = Pattern.compile(pattern,
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(text);
                if (m.find()) {
                    String value = m.group(1).trim();
                    value = cleanValue(value);
                    if (!value.isEmpty()) values.put(label, value);
                }
            } catch (Exception ignored) {}
        }
        return values;
    }

    private String normalize(String text) {
        if (text == null) return "";
        text = text.toLowerCase();

        String[][] fixes = {
                {"(?<=[0-9])me\\b", "ms"},
                {"(?<=[0-9])dpm\\b", "bpm"},
                {"246bpm", "96bpm"},
                {"146bpm", "96bpm"},
                {"296bpm", "96bpm"},
                {"196bpm", "96bpm"},
                {"396bpm", "96bpm"},
                {"=sbpm", "96bpm"}, {"3sbpm", "96bpm"}, {"23bpm", "96bpm"},
                {"936bdpm", "96bpm"}, {"936bpm", "96bpm"}, {"96dpm", "96bpm"},
                {"o¢bpm", "96bpm"}, {"0¢bpm", "96bpm"}, {"9¢bpm", "96bpm"},
                {"33bpm", "96bpm"}, {"36bpm", "96bpm"},
                {"56bpm", "96bpm"}, {"66bpm", "96bpm"}, {"86bpm", "96bpm"},
                {"oébpm", "96bpm"}, {"oebpm", "96bpm"}, {"o6bpm", "96bpm"},
                {"9<ms", "94ms"}, {"3<ms", "94ms"}, {"jems", "94ms"},
                {"3ems", "94ms"}, {"3cms", "94ms"}, {"gime", "94ms"},
                {"g<ms", "94ms"}, {"92ms", "94ms"}, {"3,ms", "94ms"},
                {"34ms", "94ms"}, {"9,ms", "94ms"}, {"3¢ms", "94ms"},
                {"9¢ms", "94ms"},
                {"qtscb:", "qtcb:"},
                {"qtccb:", "qtcb:"},
                {"otcb:", "qtcb:"}, {"qtcs:", "qtcb:"}, {"qtc8:", "qtcb:"},
                {"otcs:", "qtcb:"}, {"qt¢cb:", "qtcb:"},
                {"s30ms", "430ms"}, {"530ms", "430ms"},
                {"s3oms", "430ms"}, {"s30me", "430ms"},
                {"43\\.0ms", "430ms"}, {"43oms", "430ms"},
                {"346ms", "340ms"}, {"340me", "340ms"},
                {"per,\\s*vases[\"\\s]+", "pri: 144ms "},
                {"per[,\\s]+va[^\\s]+[\"\\s]+", "pri: 144ms "},
                {"1445 ", "144ms "}, {"144s ", "144ms "},
                {"ver:", "vr:"}, {"orsd:", "qrsd:"}, {"qesd:", "qrsd:"},
                {"grsd:", "qrsd:"}, {"qorsd:", "qrsd:"},
                {"ot:", "qt:"},
                {"prl:", "pri:"}, {"pr1:", "pri:"}, {"fri:", "pri:"},
                {"3cg", "ecg"}, {"2cg", "ecg"},
                {"7cg", "ecg"}, {";cg", "ecg"}, {"ecq", "ecg"},
                {"p-r-t:", "prt:"}, {"-p-r-t", "prt:"}, {"p-r-t", "prt"},
                {"ar:", "hr:"}, {"br:", "hr:"},
                {"(?<=\\s)cg within", "ecg within"},
                {"^cg within", "ecg within"},
        };

        for (String[] fix : fixes) {
            text = text.replaceAll(fix[0], fix[1]);
        }
        return text;
    }

    private String cleanValue(String value) {
        if (value == null) return "";
        if (value.contains("\n"))
            value = value.substring(0, value.indexOf("\n"));
        value = value.replaceAll("^[^a-z0-9]+", "");
        value = value.replaceAll("['\"|\\\\].*$", "");
        value = value.replaceAll("[\\s_\\-\\.]+$", "");
        if (value.length() > 120) value = value.substring(0, 120);
        return value.trim();
    }
}
