package com.corpcare.service;

import com.corpcare.dto.ReportAnalysisResult;
import com.corpcare.dto.ReportAnalysisResult.ParameterResult;
import com.corpcare.entity.ReportDetail;

import java.util.List;

public class ReportDetailMapper {

    public static ReportDetail fromResult(ReportAnalysisResult result, Long employeeId, String fileName) {
        ReportDetail d = new ReportDetail();
        d.setEmployeeId(employeeId);
        d.setFileName(fileName);
        d.setVendor(result.getVendor());
        d.setConfidence(result.getConfidence());
        d.setParsedCount(result.getParsedCount());
        d.setRawJson(toJson(result));

        if (result.getPatient() != null) {
            d.setPatientName(result.getPatient().getName());
            d.setPatientAge(result.getPatient().getAge());
            d.setPatientSex(result.getPatient().getSex());
            d.setPatientDate(result.getPatient().getDate());
        }

        for (ParameterResult p : result.getParameters()) {
            Double val = parseDouble(p.getValue());
            switch (p.getName()) {
                case "Haemoglobin" -> { d.setHaemoglobin(val); d.setHaemoglobinStatus(p.getStatus()); }
                case "RBC Count" -> { d.setRbcCount(val); d.setRbcCountStatus(p.getStatus()); }
                case "PCV / HCT" -> { d.setPcvHct(val); d.setPcvHctStatus(p.getStatus()); }
                case "MCV" -> { d.setMcv(val); d.setMcvStatus(p.getStatus()); }
                case "MCH" -> { d.setMch(val); d.setMchStatus(p.getStatus()); }
                case "MCHC" -> { d.setMchc(val); d.setMchcStatus(p.getStatus()); }
                case "RDW-CV" -> { d.setRdwCv(val); d.setRdwCvStatus(p.getStatus()); }
                case "Total WBC Count" -> { d.setTotalWbcCount(val); d.setTotalWbcCountStatus(p.getStatus()); }
                case "Neutrophils" -> { d.setNeutrophils(val); d.setNeutrophilsStatus(p.getStatus()); }
                case "Lymphocytes" -> { d.setLymphocytes(val); d.setLymphocytesStatus(p.getStatus()); }
                case "Monocytes" -> { d.setMonocytes(val); d.setMonocytesStatus(p.getStatus()); }
                case "Eosinophils" -> { d.setEosinophils(val); d.setEosinophilsStatus(p.getStatus()); }
                case "Basophils" -> { d.setBasophils(val); d.setBasophilsStatus(p.getStatus()); }
                case "Platelet Count" -> { d.setPlateletCount(val); d.setPlateletCountStatus(p.getStatus()); }
                case "ESR" -> { d.setEsr(val); d.setEsrStatus(p.getStatus()); }
                case "Creatinine" -> { d.setCreatinine(val); d.setCreatinineStatus(p.getStatus()); }
                case "Urine Pus Cells" -> { d.setUrinePusCells(val); d.setUrinePusCellsStatus(p.getStatus()); }
                case "Urine Protein" -> { d.setUrineProtein(p.getValue()); d.setUrineProteinStatus(p.getStatus()); }
                case "Urine Sugar" -> { d.setUrineSugar(val); d.setUrineSugarStatus(p.getStatus()); }
                case "Urine RBC" -> { d.setUrineRbc(val); d.setUrineRbcStatus(p.getStatus()); }
            }
        }

        if (!result.getAlerts().isEmpty()) {
            var critical = result.getAlerts().stream()
                .filter(a -> "HIGH".equals(a.getDirection()) || "Impossible".equals(a.getDirection()))
                .findFirst();
            if (critical.isPresent()) {
                d.setCriticalAlert("CRITICAL");
                d.setCriticalAlertMessage(critical.get().getMessage());
            } else {
                d.setCriticalAlert("WARNING");
                d.setCriticalAlertMessage(result.getAlerts().get(0).getMessage());
            }
        }

        return d;
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.replace(",", "")); }
        catch (NumberFormatException e) { return null; }
    }

    private static String toJson(Object o) {
        try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }
}
