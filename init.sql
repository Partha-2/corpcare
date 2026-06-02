CREATE DATABASE IF NOT EXISTS corpcare;

CREATE TABLE IF NOT EXISTS medical_reports (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    report_type VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(500),
    pdf_data BYTEA NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_medical_reports_employee_id ON medical_reports(employee_id);
CREATE INDEX IF NOT EXISTS idx_medical_reports_report_type ON medical_reports(report_type);