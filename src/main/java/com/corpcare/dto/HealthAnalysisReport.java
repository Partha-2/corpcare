package com.corpcare.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HealthAnalysisReport {
    private String vendorFormat;
    private String employeeName;
    private String age;
    private String sex;
    private String bloodGroup;
    private List<HealthParameter> parameters = new ArrayList<>();
    private List<String> notifications = new ArrayList<>();

    public String getVendorFormat() { return vendorFormat; }
    public void setVendorFormat(String v) { this.vendorFormat = v; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String v) { this.employeeName = v; }
    public String getAge() { return age; }
    public void setAge(String v) { this.age = v; }
    public String getSex() { return sex; }
    public void setSex(String v) { this.sex = v; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String v) { this.bloodGroup = v; }
    public List<HealthParameter> getParameters() { return parameters; }
    public void setParameters(List<HealthParameter> v) { this.parameters = v; }
    public List<String> getNotifications() { return notifications; }
    public void setNotifications(List<String> v) { this.notifications = v; }

    public Map<String, Object> toResultMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("vendorFormat", nvl(vendorFormat));
        m.put("employeeName", nvl(employeeName));
        m.put("age", nvl(age));
        m.put("sex", nvl(sex));
        m.put("bloodGroup", nvl(bloodGroup));
        m.put("notifications", notifications);
        List<Map<String, Object>> paramList = new ArrayList<>();
        for (HealthParameter p : parameters) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("name", nvl(p.getName()));
            pm.put("value", nvl(p.getValue()));
            pm.put("unit", nvl(p.getUnit()));
            pm.put("referenceRange", nvl(p.getReferenceRange()));
            pm.put("minRange", nvl(p.getMinRange()));
            pm.put("maxRange", nvl(p.getMaxRange()));
            pm.put("status", nvl(p.getStatus()));
            pm.put("recommendation", nvl(p.getRecommendation()));
            pm.put("color", nvl(p.getColor()));
            paramList.add(pm);
        }
        m.put("parameters", paramList);
        return m;
    }

    private String nvl(String v) { return v != null && !v.isBlank() ? v : "Not Available"; }
}
