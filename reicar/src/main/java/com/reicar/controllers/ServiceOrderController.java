package com.reicar.controllers;

import com.reicar.dtos.ServiceOrderDTO;
import com.reicar.services.ServiceOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/so")
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService service;

    @GetMapping("/register")
    public String showForm(@RequestParam String type, Model model) {
        // Prepara o formulário baseado no tipo (MECHANIC ou TIRE_SHOP)
        model.addAttribute("type", type);
        model.addAttribute("order", new ServiceOrderDTO());
        return "service-order-form";
    }

    @PostMapping("/register")
    public String saveOrder(@ModelAttribute ServiceOrderDTO dto, RedirectAttributes redirectAttributes) {
        service.saveFromDto(dto);
        redirectAttributes.addFlashAttribute("message", "Ordem de Serviço salva com sucesso!");

        return "redirect:/dash";
    }
}
