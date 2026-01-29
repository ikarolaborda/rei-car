package com.reicar.repositories;

import com.reicar.entities.Payment;
import com.reicar.entities.enums.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    List<Payment> findByPaymentMethod(PaymentMethod method);

    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN FETCH i.customer ORDER BY p.paymentDate DESC")
    List<Payment> findAllWithDetails();

    @Query("SELECT p FROM Payment p JOIN FETCH p.invoice i JOIN FETCH i.customer WHERE p.paymentDate BETWEEN :start AND :end ORDER BY p.paymentDate DESC")
    List<Payment> findByDateRangeWithDetails(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p JOIN p.invoice i WHERE i.customer.id = :customerId")
    BigDecimal sumPaymentsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumPaymentsBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end GROUP BY p.paymentMethod")
    List<Object[]> sumByPaymentMethodBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT CAST(p.paymentDate AS LocalDate), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end GROUP BY CAST(p.paymentDate AS LocalDate) ORDER BY CAST(p.paymentDate AS LocalDate)")
    List<Object[]> sumDailyRevenueBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
