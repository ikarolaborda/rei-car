package com.reicar.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@DiscriminatorValue("TIRE_SHOP")
public class TireShopServiceOrder extends ServiceOrder {

    private String tirePosition;
}