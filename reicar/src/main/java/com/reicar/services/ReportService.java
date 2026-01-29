package com.reicar.services;

import com.reicar.dtos.*;
import com.reicar.entities.Customer;
import com.reicar.entities.Invoice;
import com.reicar.entities.Payment;
import com.reicar.entities.enums.InvoiceStatus;
import com.reicar.entities.enums.PaymentMethod;
import com.reicar.repositories.CustomerRepository;
import com.reicar.repositories.InvoiceRepository;
import com.reicar.repositories.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;

    public DashboardMetricsDTO getDashboardMetrics() {
        LocalDate today = LocalDate.now();

        BigDecimal dailyRevenue = getRevenueForDate(today);
        BigDecimal weeklyRevenue = getRevenueForDateRange(today.minusDays(6), today);
        BigDecimal monthlyRevenue = getRevenueForDateRange(today.withDayOfMonth(1), today);

        int unpaidInvoiceCount = invoiceRepository.countByStatus(InvoiceStatus.UNPAID);
        int partialInvoiceCount = invoiceRepository.countByStatus(InvoiceStatus.PARTIAL);

        BigDecimal unpaidInvoiceTotal = invoiceRepository.sumTotalValueByStatus(InvoiceStatus.UNPAID);
        BigDecimal partialOutstandingTotal = invoiceRepository.sumPartialOutstandingBalance();
        BigDecimal totalOutstanding = invoiceRepository.sumOutstandingBalance();

        return new DashboardMetricsDTO(
            dailyRevenue,
            weeklyRevenue,
            monthlyRevenue,
            unpaidInvoiceCount,
            unpaidInvoiceTotal,
            partialInvoiceCount,
            partialOutstandingTotal,
            totalOutstanding
        );
    }

    private BigDecimal getRevenueForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return paymentRepository.sumPaymentsBetweenDates(start, end);
    }

    private BigDecimal getRevenueForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return paymentRepository.sumPaymentsBetweenDates(start, end);
    }

    public Map<PaymentMethod, BigDecimal> getRevenueByPaymentMethod(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = paymentRepository.sumByPaymentMethodBetweenDates(start, end);

        Map<PaymentMethod, BigDecimal> revenueByMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod method : PaymentMethod.values()) {
            revenueByMethod.put(method, BigDecimal.ZERO);
        }

        for (Object[] result : results) {
            PaymentMethod method = (PaymentMethod) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            revenueByMethod.put(method, amount);
        }

        return revenueByMethod;
    }

    public List<DailyRevenueDTO> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = paymentRepository.sumDailyRevenueBetweenDates(start, end);

        Map<LocalDate, BigDecimal> revenueByDate = new LinkedHashMap<>();

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            revenueByDate.put(current, BigDecimal.ZERO);
            current = current.plusDays(1);
        }

        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            revenueByDate.put(date, amount);
        }

        return revenueByDate.entrySet().stream()
            .map(entry -> new DailyRevenueDTO(entry.getKey(), entry.getValue()))
            .toList();
    }

    public List<DailyRevenueDTO> getLast30DaysRevenue() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(29);
        return getDailyRevenue(startDate, endDate);
    }

    public RevenueReportDTO generateRevenueReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Invoice> invoices = invoiceRepository.findByIssueDateBetween(startDate, endDate);

        BigDecimal totalInvoiced = invoices.stream()
            .map(Invoice::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = paymentRepository.sumPaymentsBetweenDates(start, end);

        BigDecimal outstandingBalance = invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.UNPAID || i.getStatus() == InvoiceStatus.PARTIAL)
            .map(Invoice::getRemainingBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int invoiceCount = invoices.size();
        int paidInvoiceCount = (int) invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.PAID)
            .count();
        int unpaidInvoiceCount = (int) invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.UNPAID)
            .count();
        int partialInvoiceCount = (int) invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.PARTIAL)
            .count();

        Map<PaymentMethod, BigDecimal> revenueByMethod = getRevenueByPaymentMethod(startDate, endDate);

        List<InvoiceDTO> invoiceDTOs = invoices.stream()
            .map(InvoiceDTO::from)
            .toList();

        return new RevenueReportDTO(
            startDate,
            endDate,
            LocalDate.now(),
            totalInvoiced,
            totalReceived,
            outstandingBalance,
            invoiceCount,
            paidInvoiceCount,
            unpaidInvoiceCount,
            partialInvoiceCount,
            revenueByMethod,
            invoiceDTOs
        );
    }

    public CustomerStatementDTO generateCustomerStatement(Long customerId, LocalDate startDate, LocalDate endDate) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado: " + customerId));

        List<Invoice> invoices = invoiceRepository.findByCustomerId(customerId).stream()
            .filter(i -> !i.getIssueDate().isBefore(startDate) && !i.getIssueDate().isAfter(endDate))
            .toList();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Payment> payments = invoices.stream()
            .flatMap(i -> i.getPayments().stream())
            .filter(p -> !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
            .sorted(Comparator.comparing(Payment::getPaymentDate))
            .toList();

        BigDecimal totalInvoiced = invoices.stream()
            .map(Invoice::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = payments.stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalInvoiced.subtract(totalPaid);

        List<InvoiceDTO> invoiceDTOs = invoices.stream()
            .map(InvoiceDTO::from)
            .toList();

        List<PaymentDTO> paymentDTOs = payments.stream()
            .map(PaymentDTO::from)
            .toList();

        return new CustomerStatementDTO(
            customer.getId(),
            customer.getName(),
            customer.getPhone(),
            customer.getCity(),
            customer.getState(),
            startDate,
            endDate,
            LocalDate.now(),
            invoiceDTOs,
            paymentDTOs,
            totalInvoiced,
            totalPaid,
            balance
        );
    }

    public String generateRevenueReportCsv(LocalDate startDate, LocalDate endDate) {
        RevenueReportDTO report = generateRevenueReport(startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Relatório de Receitas\n");
        csv.append("Período:;").append(startDate).append(" a ").append(endDate).append("\n");
        csv.append("Gerado em:;").append(report.generatedAt()).append("\n\n");

        csv.append("Resumo\n");
        csv.append("Total Faturado;R$ ").append(String.format("%.2f", report.totalInvoiced())).append("\n");
        csv.append("Total Recebido;R$ ").append(String.format("%.2f", report.totalReceived())).append("\n");
        csv.append("Saldo Pendente;R$ ").append(String.format("%.2f", report.outstandingBalance())).append("\n");
        csv.append("Total de Faturas;").append(report.invoiceCount()).append("\n");
        csv.append("Faturas Pagas;").append(report.paidInvoiceCount()).append("\n");
        csv.append("Faturas Pendentes;").append(report.unpaidInvoiceCount()).append("\n");
        csv.append("Faturas Parciais;").append(report.partialInvoiceCount()).append("\n\n");

        csv.append("Receitas por Método de Pagamento\n");
        for (Map.Entry<PaymentMethod, BigDecimal> entry : report.revenueByMethod().entrySet()) {
            csv.append(entry.getKey().getDisplayName()).append(";R$ ")
               .append(String.format("%.2f", entry.getValue())).append("\n");
        }
        csv.append("\n");

        csv.append("Faturas\n");
        csv.append("Número;Cliente;Data Emissão;Valor Total;Valor Pago;Status\n");
        for (InvoiceDTO invoice : report.invoices()) {
            csv.append(invoice.invoiceNumber()).append(";")
               .append(invoice.customerName()).append(";")
               .append(invoice.issueDate()).append(";")
               .append(String.format("%.2f", invoice.totalValue())).append(";")
               .append(String.format("%.2f", invoice.paidAmount())).append(";")
               .append(invoice.status()).append("\n");
        }

        return csv.toString();
    }

    public String generateCustomerStatementCsv(Long customerId, LocalDate startDate, LocalDate endDate) {
        CustomerStatementDTO statement = generateCustomerStatement(customerId, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("Extrato do Cliente\n");
        csv.append("Cliente;").append(statement.customerName()).append("\n");
        csv.append("Telefone;").append(statement.customerPhone()).append("\n");
        csv.append("Cidade/Estado;").append(statement.customerCity()).append("/").append(statement.customerState()).append("\n");
        csv.append("Período;").append(startDate).append(" a ").append(endDate).append("\n");
        csv.append("Gerado em;").append(statement.generatedAt()).append("\n\n");

        csv.append("Resumo\n");
        csv.append("Total Faturado;R$ ").append(String.format("%.2f", statement.totalInvoiced())).append("\n");
        csv.append("Total Pago;R$ ").append(String.format("%.2f", statement.totalPaid())).append("\n");
        csv.append("Saldo;R$ ").append(String.format("%.2f", statement.balance())).append("\n\n");

        csv.append("Faturas\n");
        csv.append("Número;Data Emissão;Valor Total;Valor Pago;Status\n");
        for (InvoiceDTO invoice : statement.invoices()) {
            csv.append(invoice.invoiceNumber()).append(";")
               .append(invoice.issueDate()).append(";")
               .append(String.format("%.2f", invoice.totalValue())).append(";")
               .append(String.format("%.2f", invoice.paidAmount())).append(";")
               .append(invoice.status()).append("\n");
        }
        csv.append("\n");

        csv.append("Pagamentos\n");
        csv.append("Data;Fatura;Valor;Método\n");
        for (PaymentDTO payment : statement.payments()) {
            csv.append(payment.paymentDate()).append(";")
               .append(payment.invoiceNumber()).append(";")
               .append(String.format("%.2f", payment.amount())).append(";")
               .append(payment.paymentMethod()).append("\n");
        }

        return csv.toString();
    }
}
