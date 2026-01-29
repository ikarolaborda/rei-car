package com.reicar.controllers;

import com.reicar.dtos.InvoiceDTO;
import com.reicar.entities.Invoice;
import com.reicar.entities.enums.InvoiceStatus;
import com.reicar.services.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public String listInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String search,
            Model model) {

        List<InvoiceDTO> invoices = invoiceService.findWithFilters(status, startDate, endDate, search);

        model.addAttribute("invoices", invoices);
        model.addAttribute("statuses", InvoiceStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("search", search);

        return "invoices/list";
    }

    @GetMapping("/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Invoice invoice = invoiceService.findById(id);
        model.addAttribute("invoice", InvoiceDTO.from(invoice));
        return "invoices/view";
    }

    @PostMapping("/generate/{serviceOrderId}")
    public String generateInvoice(
            @PathVariable Long serviceOrderId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Invoice invoice = invoiceService.generateFromServiceOrder(serviceOrderId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Fatura " + invoice.getInvoiceNumber() + " gerada com sucesso!");
            return "redirect:/invoices/" + invoice.getId();
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/service-orders/" + serviceOrderId;
        }
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public String cancelInvoice(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Invoice invoice = invoiceService.cancelInvoice(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Fatura " + invoice.getInvoiceNumber() + " cancelada com sucesso!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/invoices/" + id;
    }

    @GetMapping("/by-customer/{customerId}")
    public String listByCustomer(@PathVariable Long customerId, Model model) {
        List<InvoiceDTO> invoices = invoiceService.findByCustomerId(customerId);
        model.addAttribute("invoices", invoices);
        model.addAttribute("customerId", customerId);
        return "invoices/list";
    }
}
