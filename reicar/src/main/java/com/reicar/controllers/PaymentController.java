package com.reicar.controllers;

import com.reicar.dtos.PaymentDTO;
import com.reicar.dtos.PaymentFormDTO;
import com.reicar.entities.Invoice;
import com.reicar.entities.Payment;
import com.reicar.entities.enums.PaymentMethod;
import com.reicar.services.InvoiceService;
import com.reicar.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
public class PaymentController {

    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    @GetMapping
    public String listPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) PaymentMethod method,
            Model model) {

        List<PaymentDTO> payments = paymentService.findWithFilters(startDate, endDate, method);

        model.addAttribute("payments", payments);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("selectedMethod", method);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "payments/list";
    }

    @GetMapping("/new")
    public String showPaymentForm(@RequestParam Long invoiceId, Model model) {
        Invoice invoice = invoiceService.findById(invoiceId);

        model.addAttribute("invoice", invoice);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("remainingBalance", invoice.getRemainingBalance());

        return "payments/form";
    }

    @PostMapping
    public String recordPayment(
            @RequestParam Long invoiceId,
            @RequestParam BigDecimal amount,
            @RequestParam PaymentMethod paymentMethod,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            PaymentFormDTO dto = new PaymentFormDTO(invoiceId, amount, paymentMethod);
            Payment payment = paymentService.recordPayment(dto, userDetails.getUsername());

            redirectAttributes.addFlashAttribute("success",
                String.format("Pagamento de R$ %.2f registrado com sucesso!", payment.getAmount()));

            return "redirect:/invoices/" + invoiceId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/payments/new?invoiceId=" + invoiceId;
        }
    }

    @GetMapping("/by-invoice/{invoiceId}")
    public String listByInvoice(@PathVariable Long invoiceId, Model model) {
        List<PaymentDTO> payments = paymentService.findByInvoiceId(invoiceId);
        Invoice invoice = invoiceService.findById(invoiceId);

        model.addAttribute("payments", payments);
        model.addAttribute("invoice", invoice);

        return "payments/by-invoice";
    }
}
