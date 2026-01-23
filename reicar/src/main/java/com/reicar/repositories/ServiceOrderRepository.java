package com.reicar.repositories;

import com.reicar.entities.ServiceOrder;
import com.reicar.entities.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    // Busca por número da OS (ex: #0734)
    Optional<ServiceOrder> findByOrderNumber(String orderNumber);

    // Lista OS por status para alimentar os cards do dashboard
    Long countByStatus(ServiceStatus status);
}
