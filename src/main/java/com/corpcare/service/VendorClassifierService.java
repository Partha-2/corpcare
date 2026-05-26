package com.corpcare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class VendorClassifierService {

    private static final Logger log = LoggerFactory.getLogger(VendorClassifierService.class);

    private static final Pattern SHIVANI_HEADER = Pattern.compile(
        "(?i)(shivani\\s*(diagnostic|path|laboratory|lab|clinic|healthcare))|" +
        "(shivani\\s*diag|dr\\.?\\s*shivani)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STARLAB_HEADER = Pattern.compile(
        "(?i)(star\\s*lab|starlab\\s*(diagnostic|path|laboratory|lab|clinic|healthcare))|" +
        "(star\\s*laboratories|star\\s*diagnostic)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SHIVANI_SECTION = Pattern.compile(
        "(?i)(haematology|haemato|haematological|complete\\s*blood\\s*count|CBC|hemogram).*",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STARLAB_SECTION = Pattern.compile(
        "(?i)(pathology\\s*report|clinical\\s*pathology|lab\\s*report|investigation)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SHIVANI_TABLE = Pattern.compile(
        "(?i)(Parameter|Test)\\s*[|]\\s*(Result|Value|Observed)\\s*[|]\\s*(Reference|Range|Normal)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STARLAB_TABLE = Pattern.compile(
        "(?i)(Test\\s+Name\\s+Result\\s+Range|Test\\s+Result\\s+Flag\\s+Unit\\s+Ref)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SHIVANI_FOOTER = Pattern.compile(
        "(?i)(shivani|dr\\.?\\s*shivani|diagnostic\\s*centre)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern STARLAB_FOOTER = Pattern.compile(
        "(?i)(star|star\\s*lab|powered\\s*by\\s*star)",
        Pattern.CASE_INSENSITIVE
    );

    public String classify(String text) {
        if (text == null || text.isBlank()) return "GENERIC_TEMPLATE";

        int shivaniScore = 0;
        int starlabScore = 0;

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            String trimmed = line.trim();

            if (SHIVANI_HEADER.matcher(trimmed).find()) shivaniScore += 20;
            if (STARLAB_HEADER.matcher(trimmed).find()) starlabScore += 20;
            if (SHIVANI_SECTION.matcher(trimmed).find()) shivaniScore += 5;
            if (STARLAB_SECTION.matcher(trimmed).find()) starlabScore += 5;
            if (SHIVANI_TABLE.matcher(trimmed).find()) shivaniScore += 15;
            if (STARLAB_TABLE.matcher(trimmed).find()) starlabScore += 15;
            if (SHIVANI_FOOTER.matcher(trimmed).find()) shivaniScore += 10;
            if (STARLAB_FOOTER.matcher(trimmed).find()) starlabScore += 10;

            if (trimmed.toLowerCase().contains("complete blood count")
                || trimmed.toLowerCase().contains("cbc")
                || trimmed.toLowerCase().contains("hemogram")) {
                shivaniScore += 2;
            }
            if (trimmed.toLowerCase().contains("clinical pathology")
                || trimmed.toLowerCase().contains("lab report")) {
                starlabScore += 2;
            }
        }

        int totalLines = lines.length;
        if (totalLines > 0) {
            double pipeRatio = countChar(text, '|') / (double) totalLines;
            if (pipeRatio > 0.5) shivaniScore += 8;

            double colonRatio = countChar(text, ':') / (double) totalLines;
            double spaceRatio = countChar(text, ' ') / (double) totalLines;
            if (colonRatio > 1.0 && spaceRatio > 5.0) starlabScore += 8;
        }

        String result;
        if (shivaniScore > starlabScore && shivaniScore >= 15) {
            result = "SHIVANI_TEMPLATE";
        } else if (starlabScore > shivaniScore && starlabScore >= 15) {
            result = "STARLAB_TEMPLATE";
        } else {
            result = "GENERIC_TEMPLATE";
        }

        log.info("Vendor classification: {} (shivani={}, starlab={})", result, shivaniScore, starlabScore);
        return result;
    }

    private int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) count++;
        }
        return count;
    }
}
