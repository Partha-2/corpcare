package com.corpcare.pdfsplit.model.response;

import lombok.Data;
import java.util.Map;

@Data
public class AnalysisResult {
    private String type;
    private int pageNumber;
    private String confidence;
    private String ocrEngine;
    private Map<String, String> values;
    private int valuesExtracted;
    private String jsonFile;
    private String pdfFile;
    private String viewUrl;
    private String downloadUrl;
    private String deleteUrl;
}
