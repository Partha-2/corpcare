package com.corpcare.dto;

public class DashboardStats {
    private long clients;
    private long hospitals;
    private long employees;
    private long slots;

    public DashboardStats(long clients, long hospitals, long employees, long slots) {
        this.clients = clients;
        this.hospitals = hospitals;
        this.employees = employees;
        this.slots = slots;
    }

    public long getClients() { return clients; }
    public long getHospitals() { return hospitals; }
    public long getEmployees() { return employees; }
    public long getSlots() { return slots; }
}
