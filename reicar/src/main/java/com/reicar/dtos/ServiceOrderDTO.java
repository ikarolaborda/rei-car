package com.reicar.dtos;

import java.util.ArrayList;
import java.util.List;

public record ServiceOrderDTO(
        String customerName,
        String customerPhone,
        String customerCity,
        String customerState,
        String type,
        String technicalDiagnosis,
        Integer vehicleKm,
        String tirePosition,
        List<ServiceItemDTO> items
) {
    public ServiceOrderDTO() {
        this(null, null, "Sol Nascente", "DF", null, null, null, null, new ArrayList<>());
    }
}
