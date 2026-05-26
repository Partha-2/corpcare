-- Supabase migration: Create report_detail table with individual parameter columns
-- Run this in Supabase SQL Editor (Dashboard → SQL Editor)

CREATE TABLE IF NOT EXISTS report_detail (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    file_name TEXT,
    vendor TEXT,
    confidence TEXT,
    parsed_count INT DEFAULT 0,

    patient_name TEXT,
    patient_age TEXT,
    patient_sex TEXT,
    patient_date TEXT,

    haemoglobin DOUBLE PRECISION, haemoglobin_status TEXT,
    rbc_count DOUBLE PRECISION, rbc_count_status TEXT,
    pcv_hct DOUBLE PRECISION, pcv_hct_status TEXT,
    mcv DOUBLE PRECISION, mcv_status TEXT,
    mch DOUBLE PRECISION, mch_status TEXT,
    mchc DOUBLE PRECISION, mchc_status TEXT,
    rdw_cv DOUBLE PRECISION, rdw_cv_status TEXT,
    total_wbc_count DOUBLE PRECISION, total_wbc_count_status TEXT,
    neutrophils DOUBLE PRECISION, neutrophils_status TEXT,
    lymphocytes DOUBLE PRECISION, lymphocytes_status TEXT,
    monocytes DOUBLE PRECISION, monocytes_status TEXT,
    eosinophils DOUBLE PRECISION, eosinophils_status TEXT,
    basophils DOUBLE PRECISION, basophils_status TEXT,
    platelet_count DOUBLE PRECISION, platelet_count_status TEXT,
    esr DOUBLE PRECISION, esr_status TEXT,
    creatinine DOUBLE PRECISION, creatinine_status TEXT,
    urine_pus_cells DOUBLE PRECISION, urine_pus_cells_status TEXT,
    urine_protein TEXT, urine_protein_status TEXT,
    urine_sugar DOUBLE PRECISION, urine_sugar_status TEXT,
    urine_rbc DOUBLE PRECISION, urine_rbc_status TEXT,

    critical_alert TEXT,
    critical_alert_message TEXT,
    raw_json TEXT,

    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_report_detail_employee ON report_detail(employee_id);
CREATE INDEX IF NOT EXISTS idx_report_detail_created ON report_detail(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_report_detail_hb_status ON report_detail(haemoglobin_status);
CREATE INDEX IF NOT EXISTS idx_report_detail_rbc_status ON report_detail(rbc_count_status);
CREATE INDEX IF NOT EXISTS idx_report_detail_wbc_status ON report_detail(total_wbc_count_status);
CREATE INDEX IF NOT EXISTS idx_report_detail_esr_status ON report_detail(esr_status);

-- Optional: View that flattens everything for analytics
CREATE OR REPLACE VIEW report_analytics AS
SELECT
    id, employee_id, created_at,
    patient_name, patient_age, patient_sex,
    vendor, confidence, parsed_count,
    haemoglobin, haemoglobin_status,
    rbc_count, rbc_count_status,
    pcv_hct, pcv_hct_status,
    mcv, mcv_status,
    mch, mch_status,
    mchc, mchc_status,
    rdw_cv, rdw_cv_status,
    total_wbc_count, total_wbc_count_status,
    neutrophils, neutrophils_status,
    lymphocytes, lymphocytes_status,
    monocytes, monocytes_status,
    eosinophils, eosinophils_status,
    basophils, basophils_status,
    platelet_count, platelet_count_status,
    esr, esr_status,
    creatinine, creatinine_status,
    urine_pus_cells, urine_pus_cells_status,
    urine_protein, urine_protein_status,
    urine_sugar, urine_sugar_status,
    urine_rbc, urine_rbc_status,
    critical_alert, critical_alert_message
FROM report_detail;
