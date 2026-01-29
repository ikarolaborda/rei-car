package com.reicar.services;

import com.reicar.dtos.PaymentDTO;
import com.reicar.dtos.PaymentFormDTO;
import com.reicar.entities.Invoice;
import com.reicar.entities.Payment;
import com.reicar.entities.enums.InvoiceStatus;
import com.reicar.entities.enums.PaymentMethod;
import com.reicar.repositories.InvoiceRepository;
import com.reicar.repositories.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    public Payment recordPayment(PaymentFormDTO dto, String username) {
        Invoice invoice = invoiceRepository.findById(dto.invoiceId())
            .orElseThrow(() -> new EntityNotFoundException("Fatura não encontrada: " + dto.invoiceId()));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Não é possível registrar pagamento para fatura cancelada");
        }

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Esta fatura já está totalmente paga");
        }

        if (dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
        }

        BigDecimal remainingBalance = invoice.getRemainingBalance();
        if (dto.amount().compareTo(remainingBalance) > 0) {
            throw new IllegalArgumentException(
                String.format("Valor do pagamento (R$ %.2f) excede o saldo restante (R$ %.2f)",
                    dto.amount(), remainingBalance));
        }

        Payment payment = Payment.builder()
            .invoice(invoice)
            .amount(dto.amount())
            .paymentDate(LocalDateTime.now())
            .paymentMethod(dto.paymentMethod())
            .recordedBy(username)
            .build();

        payment = paymentRepository.save(payment);

        invoice.addPayment(payment);
        updateInvoiceStatusBasedOnPayment(invoice, username);

        return payment;
    }

    private void updateInvoiceStatusBasedOnPayment(Invoice invoice, String username) {
        BigDecimal remainingBalance = invoice.getRemainingBalance();
        InvoiceStatus newStatus;

        if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            newStatus = InvoiceStatus.PAID;
        } else if (invoice.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            newStatus = InvoiceStatus.PARTIAL;
        } else {
            newStatus = InvoiceStatus.UNPAID;
        }

        invoiceService.updateInvoiceStatus(invoice, newStatus, username);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> findByInvoiceId(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
            .map(PaymentDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> findAll() {
        return paymentRepository.findAllWithDetails().stream()
            .map(PaymentDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return paymentRepository.findByDateRangeWithDetails(start, end).stream()
            .map(PaymentDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> findByPaymentMethod(PaymentMethod method) {
        return paymentRepository.findByPaymentMethod(method).stream()
            .map(PaymentDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> findWithFilters(LocalDate startDate, LocalDate endDate, PaymentMethod method) {
        if (startDate != null && endDate != null) {
            return findByDateRange(startDate, endDate);
        }

        if (method != null) {
            return findByPaymentMethod(method);
        }

        return findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidByCustomer(Long customerId) {
        BigDecimal total = paymentRepository.sumPaymentsByCustomerId(customerId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstandingByCustomer(Long customerId) {
        BigDecimal total = invoiceRepository.sumOutstandingByCustomerId(customerId);
        return total != null ? total : BigDecimal.ZERO;
    }
}
