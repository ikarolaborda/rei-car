package com.reicar.controllers;

import com.reicar.dtos.CustomerStatementDTO;
import com.reicar.dtos.DailyRevenueDTO;
import com.reicar.dtos.DashboardMetricsDTO;
import com.reicar.dtos.RevenueReportDTO;
import com.reicar.entities.Customer;
import com.reicar.repositories.CustomerRepository;
import com.reicar.services.PdfExportService;
import com.reicar.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final PdfExportService pdfExportService;
    private final CustomerRepository customerRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
    public String dashboard(Model model) {
        DashboardMetricsDTO metrics = reportService.getDashboardMetrics();
        List<DailyRevenueDTO> dailyRevenue = reportService.getLast30DaysRevenue();

        model.addAttribute("metrics", metrics);
        model.addAttribute("dailyRevenue", dailyRevenue);

        return "reports/dashboard";
    }

    @GetMapping("/revenue")
    public String revenueReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        RevenueReportDTO report = reportService.generateRevenueReport(startDate, endDate);

        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "reports/revenue";
    }

    @GetMapping("/revenue/pdf")
    public ResponseEntity<byte[]> exportRevenueReportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] pdfContent = pdfExportService.generateRevenueReportPdf(startDate, endDate);

        String filename = String.format("relatorio-receitas-%s-%s.pdf", startDate, endDate);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfContent);
    }

    @GetMapping("/revenue/csv")
    public ResponseEntity<String> exportRevenueReportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csvContent = reportService.generateRevenueReportCsv(startDate, endDate);

        String filename = String.format("relatorio-receitas-%s-%s.csv", startDate, endDate);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvContent);
    }

    @GetMapping("/customer-statement")
    public String customerStatementForm(Model model) {
        List<Customer> customers = customerRepository.findAll();
        model.addAttribute("customers", customers);
        return "reports/customer-statement-form";
    }

    @GetMapping("/customer-statement/{customerId}")
    public String customerStatement(
            @PathVariable Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(3);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        CustomerStatementDTO statement = reportService.generateCustomerStatement(customerId, startDate, endDate);

        model.addAttribute("statement", statement);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "reports/customer-statement";
    }

    @GetMapping("/customer-statement/{customerId}/pdf")
    public ResponseEntity<byte[]> exportCustomerStatementPdf(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        byte[] pdfContent = pdfExportService.generateCustomerStatementPdf(customerId, startDate, endDate);

        String filename = String.format("extrato-cliente-%d-%s-%s.pdf", customerId, startDate, endDate);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfContent);
    }

    @GetMapping("/customer-statement/{customerId}/csv")
    public ResponseEntity<String> exportCustomerStatementCsv(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csvContent = reportService.generateCustomerStatementCsv(customerId, startDate, endDate);

        String filename = String.format("extrato-cliente-%d-%s-%s.csv", customerId, startDate, endDate);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvContent);
    }
}
