package com.reicar.repositories;

import com.reicar.entities.ServiceOrder;
import com.reicar.entities.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {
    // Busca por número da OS (ex: #0734)
    Optional<ServiceOrder> findByOrderNumber(String orderNumber);

    // Lista OS por status para alimentar os cards do dashboard
    Long countByStatus(ServiceStatus status);

    @Query("SELECT so FROM ServiceOrder so JOIN FETCH so.customer")
    List<ServiceOrder> findAllWithCustomer();

    @Query("SELECT s FROM ServiceOrder s LEFT JOIN FETCH s.items LEFT JOIN FETCH s.customer WHERE s.id = :id")
    Optional<ServiceOrder> findByIdWithDetails(@Param("id") Long id);
}
