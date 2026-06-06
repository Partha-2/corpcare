package com.corpcare.pdfanalyzer.model.response;

import lombok.Data;

@Data
public class ReportParameter {
    private String name;
    private String value;
    private String unit;
    private String referenceRange;
    private String status;
    private String category;

    public static ReportParameter of(String name, String value, String unit,
                                      String referenceRange, String status, String category) {
        ReportParameter p = new ReportParameter();
        p.setName(name);
        p.setValue(value);
        p.setUnit(unit);
        p.setReferenceRange(referenceRange);
        p.setStatus(status);
        p.setCategory(category);
        return p;
    }
}
