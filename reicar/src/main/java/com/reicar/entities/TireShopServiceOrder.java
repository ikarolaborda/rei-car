package com.reicar.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TIRE_SHOP")
public class TireShopServiceOrder extends ServiceOrder {

    private String tirePosition; // Ex: Front Right, Rear Left
}