package com.corpcare.service;

import com.corpcare.dto.HealthParameter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HealthRecommendationService {

    public List<String> generateNotifications(List<HealthParameter> parameters) {
        List<String> notifications = new ArrayList<>();

        for (HealthParameter p : parameters) {
            if ("ABOVE_RANGE".equals(p.getStatus())) {
                notifications.add("Critical Health Value Detected: " + p.getName() + " (" + p.getValue() + ") " +
                    p.getUnit() + " — " + p.getRecommendation());
            } else if ("BELOW_RANGE".equals(p.getStatus())) {
                notifications.add("Correction Recommended: " + p.getName() + " (" + p.getValue() + ") " +
                    p.getUnit() + " — " + p.getRecommendation());
            }
        }

        return notifications;
    }

    public String generateReportText(String vendorFormat, String name, String age, String sex,
                                      String bloodGroup, List<HealthParameter> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append("---------------------------------------\n");
        sb.append("EMPLOYEE HEALTH ANALYSIS REPORT\n");
        sb.append("---------------------------------------\n\n");
        sb.append("Vendor Format Detected: ").append(vendorFormat).append("\n\n");
        sb.append("Employee Details:\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Age: ").append(age).append("\n");
        sb.append("Sex: ").append(sex).append("\n");
        sb.append("Blood Group: ").append(bloodGroup).append("\n\n");
        sb.append("Health Parameters:\n");
        sb.append("--------------------------------------------------------------------\n");
        sb.append(String.format("%-25s %-15s %-20s %s", "Parameter", "Current Value", "Range", "Status"));
        sb.append("\n--------------------------------------------------------------------\n");

        for (HealthParameter p : parameters) {
            if ("NOT_AVAILABLE".equals(p.getStatus())) continue;
            String val = p.getValue() + " " + p.getUnit();
            String range = p.getReferenceRange();
            if (range == null || range.isBlank() || "null - null".equals(range)) range = "N/A";
            sb.append(String.format("%-25s %-15s %-20s %s",
                p.getName(), val, range, p.getStatus()));
            sb.append("\n");
        }

        sb.append("--------------------------------------------------------------------\n\n");
        sb.append("Recommendations:\n");
        for (HealthParameter p : parameters) {
            if (!"NORMAL".equals(p.getStatus()) && !"NOT_AVAILABLE".equals(p.getStatus())) {
                sb.append("- ").append(p.getName()).append(": ").append(p.getRecommendation()).append("\n");
            }
        }

        sb.append("\n--- End of Report ---\n");
        return sb.toString();
    }
}
