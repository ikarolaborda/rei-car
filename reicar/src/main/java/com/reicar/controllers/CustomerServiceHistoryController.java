package com.reicar.controllers;

import com.reicar.entities.ServiceOrder;
import com.reicar.entities.User;
import com.reicar.services.ServiceOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/my-services")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerServiceHistoryController {

    private final ServiceOrderService serviceOrderService;

    @GetMapping
    public String myServices(Model model,
                             @AuthenticationPrincipal User user,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             @RequestParam(defaultValue = "date") String sortBy) {

        if (user.getCustomer() == null) {
            throw new AccessDeniedException("Usuário não possui cliente vinculado");
        }

        var orders = serviceOrderService.findByCustomerWithFilters(
            user.getCustomer(), startDate, endDate, sortBy);

        int warrantyDays = serviceOrderService.getWarrantyDays();

        model.addAttribute("orders", orders);
        model.addAttribute("customer", user.getCustomer());
        model.addAttribute("warrantyDays", warrantyDays);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("sortBy", sortBy);

        return "customer/service-history";
    }

    @GetMapping("/{id}")
    public String serviceDetail(@PathVariable Long id,
                                Model model,
                                @AuthenticationPrincipal User user) {

        if (user.getCustomer() == null) {
            throw new AccessDeniedException("Usuário não possui cliente vinculado");
        }

        ServiceOrder order = serviceOrderService.findByIdForCustomer(id, user.getCustomer())
            .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada"));

        int warrantyDays = serviceOrderService.getWarrantyDays();

        model.addAttribute("order", order);
        model.addAttribute("warrantyDays", warrantyDays);
        model.addAttribute("isUnderWarranty", order.isUnderWarranty(warrantyDays));

        return "customer/service-detail";
    }

    @PostMapping("/{id}/claim-warranty")
    public String claimWarranty(@PathVariable Long id,
                                @RequestParam String reason,
                                @AuthenticationPrincipal User user,
                                RedirectAttributes redirectAttributes) {

        if (user.getCustomer() == null) {
            throw new AccessDeniedException("Usuário não possui cliente vinculado");
        }

        try {
            serviceOrderService.claimWarranty(id, user.getCustomer(), reason);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitação de garantia registrada com sucesso!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ordem de serviço não encontrada");
        }

        return "redirect:/my-services/" + id;
    }
}
