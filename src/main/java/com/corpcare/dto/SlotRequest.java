package com.corpcare.dto;

import com.corpcare.enums.ShiftType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class SlotRequest {
    @NotNull
    private LocalDate slotDate;

    @NotNull
    private ShiftType shiftType;

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }
    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = shiftType; }
}
