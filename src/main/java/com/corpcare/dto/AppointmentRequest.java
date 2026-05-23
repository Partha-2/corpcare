package com.corpcare.dto;

import jakarta.validation.constraints.NotNull;

public class AppointmentRequest {
    @NotNull
    private Long employeeId;

    @NotNull
    private Long slotId;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
}
