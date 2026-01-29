package com.reicar.services;

import com.reicar.dtos.InvoiceDTO;
import com.reicar.entities.Invoice;
import com.reicar.entities.InvoiceStatusHistory;
import com.reicar.entities.ServiceOrder;
import com.reicar.entities.enums.InvoiceStatus;
import com.reicar.entities.enums.ServiceStatus;
import com.reicar.repositories.InvoiceRepository;
import com.reicar.repositories.InvoiceStatusHistoryRepository;
import com.reicar.repositories.ServiceOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceStatusHistoryRepository statusHistoryRepository;
    private final ServiceOrderRepository serviceOrderRepository;

    public Invoice generateFromServiceOrder(Long serviceOrderId, String username) {
        ServiceOrder serviceOrder = serviceOrderRepository.findByIdWithDetails(serviceOrderId)
            .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada: " + serviceOrderId));

        if (serviceOrder.getStatus() != ServiceStatus.FINISHED) {
            throw new IllegalStateException("Não é possível gerar fatura: Ordem de serviço não está finalizada");
        }

        if (invoiceRepository.existsByServiceOrderId(serviceOrderId)) {
            throw new IllegalStateException("Já existe uma fatura para esta ordem de serviço");
        }

        Invoice invoice = Invoice.builder()
            .invoiceNumber(generateNextInvoiceNumber())
            .issueDate(LocalDate.now())
            .status(InvoiceStatus.UNPAID)
            .serviceOrder(serviceOrder)
            .customer(serviceOrder.getCustomer())
            .totalValue(serviceOrder.getTotalValue())
            .build();

        invoice = invoiceRepository.save(invoice);

        recordStatusHistory(invoice, null, InvoiceStatus.UNPAID, username);

        return invoice;
    }

    private String generateNextInvoiceNumber() {
        long count = invoiceRepository.count() + 1;
        return String.format("FAT-%d-%04d", LocalDate.now().getYear(), count);
    }

    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        return invoiceRepository.findByIdWithCustomer(id)
            .orElseThrow(() -> new EntityNotFoundException("Fatura não encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> findAll() {
        return invoiceRepository.findAllWithDetails().stream()
            .map(InvoiceDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> findByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
            .map(InvoiceDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByIssueDateBetween(startDate, endDate).stream()
            .map(InvoiceDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> findByCustomerId(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId).stream()
            .map(InvoiceDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> searchByCustomerNameOrInvoiceNumber(String query) {
        return invoiceRepository.searchByCustomerNameOrInvoiceNumber(query).stream()
            .map(InvoiceDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceDTO> findWithFilters(InvoiceStatus status, LocalDate startDate, LocalDate endDate, String search) {
        if (search != null && !search.isBlank()) {
            return searchByCustomerNameOrInvoiceNumber(search);
        }

        if (status != null && startDate != null && endDate != null) {
            return invoiceRepository.findByStatusAndDateRange(status, startDate, endDate).stream()
                .map(InvoiceDTO::from)
                .toList();
        }

        if (status != null) {
            return findByStatus(status);
        }

        if (startDate != null && endDate != null) {
            return findByDateRange(startDate, endDate);
        }

        return findAll();
    }

    public Invoice cancelInvoice(Long invoiceId, String username) {
        Invoice invoice = findById(invoiceId);

        if (invoice.hasPayments()) {
            throw new IllegalStateException("Não é possível cancelar fatura com pagamentos registrados");
        }

        InvoiceStatus previousStatus = invoice.getStatus();
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice = invoiceRepository.save(invoice);

        recordStatusHistory(invoice, previousStatus, InvoiceStatus.CANCELLED, username);

        return invoice;
    }

    public void updateInvoiceStatus(Invoice invoice, InvoiceStatus newStatus, String username) {
        InvoiceStatus previousStatus = invoice.getStatus();
        if (previousStatus != newStatus) {
            invoice.setStatus(newStatus);
            invoiceRepository.save(invoice);
            recordStatusHistory(invoice, previousStatus, newStatus, username);
        }
    }

    private void recordStatusHistory(Invoice invoice, InvoiceStatus previousStatus, InvoiceStatus newStatus, String username) {
        InvoiceStatusHistory history = InvoiceStatusHistory.builder()
            .invoice(invoice)
            .previousStatus(previousStatus != null ? previousStatus : newStatus)
            .newStatus(newStatus)
            .changedAt(LocalDateTime.now())
            .changedBy(username)
            .build();

        statusHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public boolean canGenerateInvoice(Long serviceOrderId) {
        var serviceOrder = serviceOrderRepository.findById(serviceOrderId);
        if (serviceOrder.isEmpty()) {
            return false;
        }

        if (serviceOrder.get().getStatus() != ServiceStatus.FINISHED) {
            return false;
        }

        return !invoiceRepository.existsByServiceOrderId(serviceOrderId);
    }

    @Transactional(readOnly = true)
    public Invoice findByServiceOrderId(Long serviceOrderId) {
        return invoiceRepository.findByServiceOrderId(serviceOrderId).orElse(null);
    }
}
