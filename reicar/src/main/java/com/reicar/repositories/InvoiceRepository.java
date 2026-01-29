package com.reicar.repositories;

import com.reicar.entities.Invoice;
import com.reicar.entities.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByServiceOrderId(Long serviceOrderId);

    boolean existsByServiceOrderId(Long serviceOrderId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);

    List<Invoice> findByCustomerId(Long customerId);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.customer WHERE i.id = :id")
    Optional<Invoice> findByIdWithCustomer(@Param("id") Long id);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.customer LEFT JOIN FETCH i.serviceOrder")
    List<Invoice> findAllWithDetails();

    @Query("SELECT i FROM Invoice i JOIN FETCH i.customer WHERE i.customer.name LIKE %:query% OR i.invoiceNumber LIKE %:query%")
    List<Invoice> searchByCustomerNameOrInvoiceNumber(@Param("query") String query);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.customer WHERE i.status = :status AND i.issueDate BETWEEN :startDate AND :endDate")
    List<Invoice> findByStatusAndDateRange(
        @Param("status") InvoiceStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    int countByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM Invoice i WHERE i.status = :status")
    BigDecimal sumTotalValueByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.totalValue - i.paidAmount), 0) FROM Invoice i WHERE i.status IN ('UNPAID', 'PARTIAL')")
    BigDecimal sumOutstandingBalance();

    @Query("SELECT COALESCE(SUM(i.totalValue - i.paidAmount), 0) FROM Invoice i WHERE i.status = 'PARTIAL'")
    BigDecimal sumPartialOutstandingBalance();

    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM Invoice i WHERE i.customer.id = :customerId AND i.status IN ('UNPAID', 'PARTIAL')")
    BigDecimal sumOutstandingByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(i.totalValue), 0) FROM Invoice i WHERE i.customer.id = :customerId")
    BigDecimal sumTotalInvoicedByCustomerId(@Param("customerId") Long customerId);
}
