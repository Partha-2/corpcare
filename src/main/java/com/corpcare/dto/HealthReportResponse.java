package com.corpcare.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class HealthReportResponse {
    private String name;
    private String age;
    private String sex;
    private String bloodGroup;
    private String height;
    private String weight;
    private String bmi;
    private String heightStatus;
    private String weightStatus;
    private String recommendedWeightMin;
    private String recommendedWeightMax;
    private String bloodPressureSystolic;
    private String bloodPressureDiastolic;
    private String bpStatus;
    private String bloodSugarFasting;
    private String bloodSugarPostPrandial;
    private String bloodSugarRandom;
    private String sugarStatus;
    private String hemoglobin;
    private String hemoglobinStatus;
    private String rbcCount;
    private String wbcCount;
    private String plateletCount;
    private String totalCholesterol;
    private String cholesterolStatus;
    private String hdlCholesterol;
    private String ldlCholesterol;
    private String triglycerides;
    private String serumCreatinine;
    private String creatinineStatus;
    private String urea;
    private String uricAcid;
    private String pulseRate;
    private String oxygenSaturation;
    private String temperature;
    private String vitaminD;
    private String vitaminB12;
    private String tsh;
    private String esr;
    private String totalBilirubin;
    private String totalProtein;

    public Map<String, Object> toResultMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", nvl(name));
        m.put("age", nvl(age));
        m.put("sex", nvl(sex));
        m.put("bloodGroup", nvl(bloodGroup));
        m.put("height", nvl(height));
        m.put("weight", nvl(weight));
        m.put("bmi", nvl(bmi));
        m.put("heightStatus", nvl(heightStatus));
        m.put("weightStatus", nvl(weightStatus));
        m.put("recommendedWeightMin", nvl(recommendedWeightMin));
        m.put("recommendedWeightMax", nvl(recommendedWeightMax));
        m.put("bloodPressureSystolic", nvl(bloodPressureSystolic));
        m.put("bloodPressureDiastolic", nvl(bloodPressureDiastolic));
        m.put("bpStatus", nvl(bpStatus));
        m.put("bloodSugarFasting", nvl(bloodSugarFasting));
        m.put("bloodSugarPostPrandial", nvl(bloodSugarPostPrandial));
        m.put("bloodSugarRandom", nvl(bloodSugarRandom));
        m.put("sugarStatus", nvl(sugarStatus));
        m.put("hemoglobin", nvl(hemoglobin));
        m.put("hemoglobinStatus", nvl(hemoglobinStatus));
        m.put("rbcCount", nvl(rbcCount));
        m.put("wbcCount", nvl(wbcCount));
        m.put("plateletCount", nvl(plateletCount));
        m.put("totalCholesterol", nvl(totalCholesterol));
        m.put("cholesterolStatus", nvl(cholesterolStatus));
        m.put("hdlCholesterol", nvl(hdlCholesterol));
        m.put("ldlCholesterol", nvl(ldlCholesterol));
        m.put("triglycerides", nvl(triglycerides));
        m.put("serumCreatinine", nvl(serumCreatinine));
        m.put("creatinineStatus", nvl(creatinineStatus));
        m.put("urea", nvl(urea));
        m.put("uricAcid", nvl(uricAcid));
        m.put("pulseRate", nvl(pulseRate));
        m.put("oxygenSaturation", nvl(oxygenSaturation));
        m.put("temperature", nvl(temperature));
        m.put("vitaminD", nvl(vitaminD));
        m.put("vitaminB12", nvl(vitaminB12));
        m.put("tsh", nvl(tsh));
        m.put("esr", nvl(esr));
        m.put("totalBilirubin", nvl(totalBilirubin));
        m.put("totalProtein", nvl(totalProtein));
        return m;
    }

    private String nvl(String v) { return v != null && !v.isBlank() ? v : "N/A"; }

    public String getName() { return name; }
    public void setName(String v) { this.name = v; }
    public String getAge() { return age; }
    public void setAge(String v) { this.age = v; }
    public String getSex() { return sex; }
    public void setSex(String v) { this.sex = v; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String v) { this.bloodGroup = v; }
    public String getHeight() { return height; }
    public void setHeight(String v) { this.height = v; }
    public String getWeight() { return weight; }
    public void setWeight(String v) { this.weight = v; }
    public String getBmi() { return bmi; }
    public void setBmi(String v) { this.bmi = v; }
    public String getHeightStatus() { return heightStatus; }
    public void setHeightStatus(String v) { this.heightStatus = v; }
    public String getWeightStatus() { return weightStatus; }
    public void setWeightStatus(String v) { this.weightStatus = v; }
    public String getRecommendedWeightMin() { return recommendedWeightMin; }
    public void setRecommendedWeightMin(String v) { this.recommendedWeightMin = v; }
    public String getRecommendedWeightMax() { return recommendedWeightMax; }
    public void setRecommendedWeightMax(String v) { this.recommendedWeightMax = v; }
    public String getBloodPressureSystolic() { return bloodPressureSystolic; }
    public void setBloodPressureSystolic(String v) { this.bloodPressureSystolic = v; }
    public String getBloodPressureDiastolic() { return bloodPressureDiastolic; }
    public void setBloodPressureDiastolic(String v) { this.bloodPressureDiastolic = v; }
    public String getBpStatus() { return bpStatus; }
    public void setBpStatus(String v) { this.bpStatus = v; }
    public String getBloodSugarFasting() { return bloodSugarFasting; }
    public void setBloodSugarFasting(String v) { this.bloodSugarFasting = v; }
    public String getBloodSugarPostPrandial() { return bloodSugarPostPrandial; }
    public void setBloodSugarPostPrandial(String v) { this.bloodSugarPostPrandial = v; }
    public String getBloodSugarRandom() { return bloodSugarRandom; }
    public void setBloodSugarRandom(String v) { this.bloodSugarRandom = v; }
    public String getSugarStatus() { return sugarStatus; }
    public void setSugarStatus(String v) { this.sugarStatus = v; }
    public String getHemoglobin() { return hemoglobin; }
    public void setHemoglobin(String v) { this.hemoglobin = v; }
    public String getHemoglobinStatus() { return hemoglobinStatus; }
    public void setHemoglobinStatus(String v) { this.hemoglobinStatus = v; }
    public String getRbcCount() { return rbcCount; }
    public void setRbcCount(String v) { this.rbcCount = v; }
    public String getWbcCount() { return wbcCount; }
    public void setWbcCount(String v) { this.wbcCount = v; }
    public String getPlateletCount() { return plateletCount; }
    public void setPlateletCount(String v) { this.plateletCount = v; }
    public String getTotalCholesterol() { return totalCholesterol; }
    public void setTotalCholesterol(String v) { this.totalCholesterol = v; }
    public String getCholesterolStatus() { return cholesterolStatus; }
    public void setCholesterolStatus(String v) { this.cholesterolStatus = v; }
    public String getHdlCholesterol() { return hdlCholesterol; }
    public void setHdlCholesterol(String v) { this.hdlCholesterol = v; }
    public String getLdlCholesterol() { return ldlCholesterol; }
    public void setLdlCholesterol(String v) { this.ldlCholesterol = v; }
    public String getTriglycerides() { return triglycerides; }
    public void setTriglycerides(String v) { this.triglycerides = v; }
    public String getSerumCreatinine() { return serumCreatinine; }
    public void setSerumCreatinine(String v) { this.serumCreatinine = v; }
    public String getCreatinineStatus() { return creatinineStatus; }
    public void setCreatinineStatus(String v) { this.creatinineStatus = v; }
    public String getUrea() { return urea; }
    public void setUrea(String v) { this.urea = v; }
    public String getUricAcid() { return uricAcid; }
    public void setUricAcid(String v) { this.uricAcid = v; }
    public String getPulseRate() { return pulseRate; }
    public void setPulseRate(String v) { this.pulseRate = v; }
    public String getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(String v) { this.oxygenSaturation = v; }
    public String getTemperature() { return temperature; }
    public void setTemperature(String v) { this.temperature = v; }
    public String getVitaminD() { return vitaminD; }
    public void setVitaminD(String v) { this.vitaminD = v; }
    public String getVitaminB12() { return vitaminB12; }
    public void setVitaminB12(String v) { this.vitaminB12 = v; }
    public String getTsh() { return tsh; }
    public void setTsh(String v) { this.tsh = v; }
    public String getEsr() { return esr; }
    public void setEsr(String v) { this.esr = v; }
    public String getTotalBilirubin() { return totalBilirubin; }
    public void setTotalBilirubin(String v) { this.totalBilirubin = v; }
    public String getTotalProtein() { return totalProtein; }
    public void setTotalProtein(String v) { this.totalProtein = v; }
}
