package com.reicar.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("MECHANIC")
public class MechanicServiceOrder extends ServiceOrder {

    private String technicalDiagnosis;
    private Integer vehicleKm;
}
