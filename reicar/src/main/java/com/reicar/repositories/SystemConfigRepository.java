package com.reicar.repositories;

import com.reicar.entities.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    default int getWarrantyDays() {
        return findById("warranty_days")
            .map(config -> Integer.parseInt(config.getConfigValue()))
            .orElse(90);
    }
}
