package com.reicar.controllers;

import com.reicar.entities.SystemConfig;
import com.reicar.repositories.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final SystemConfigRepository configRepository;

    @GetMapping
    public String showSettings(Model model) {
        int warrantyDays = configRepository.getWarrantyDays();
        model.addAttribute("warrantyDays", warrantyDays);
        return "admin/settings";
    }

    @PostMapping("/warranty")
    public String updateWarrantyDays(@RequestParam int warrantyDays, RedirectAttributes redirectAttributes) {
        if (warrantyDays < 0 || warrantyDays > 365) {
            redirectAttributes.addFlashAttribute("errorMessage", "Período de garantia deve estar entre 0 e 365 dias");
            return "redirect:/admin/settings";
        }

        SystemConfig config = configRepository.findById("warranty_days")
            .orElse(new SystemConfig("warranty_days", "90", "Período de garantia em dias"));

        config.setConfigValue(String.valueOf(warrantyDays));
        configRepository.save(config);

        redirectAttributes.addFlashAttribute("successMessage", "Período de garantia atualizado para " + warrantyDays + " dias");
        return "redirect:/admin/settings";
    }
}
