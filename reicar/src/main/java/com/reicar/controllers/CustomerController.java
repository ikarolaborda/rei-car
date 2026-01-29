package com.reicar.controllers;

import com.reicar.entities.Customer;
import com.reicar.repositories.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MECHANIC')")
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model) {
        var customers = (search != null && !search.isBlank())
                ? customerRepository.findByNameContainingIgnoreCase(search)
                : customerRepository.findAll();
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        return "customers/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("editing", false);
        return "customers/form";
    }

    @PostMapping
    public String createCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        customerRepository.save(customer);
        redirectAttributes.addFlashAttribute("successMessage", "Cliente cadastrado com sucesso!");
        return "redirect:/customers";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        model.addAttribute("customer", customer);
        model.addAttribute("editing", true);
        return "customers/form";
    }

    @PostMapping("/{id}")
    public String updateCustomer(@PathVariable Long id,
                                 @ModelAttribute Customer customer,
                                 RedirectAttributes redirectAttributes) {
        var existing = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        existing.setName(customer.getName());
        existing.setPhone(customer.getPhone());
        existing.setCity(customer.getCity());
        existing.setState(customer.getState());

        customerRepository.save(existing);
        redirectAttributes.addFlashAttribute("successMessage", "Cliente atualizado com sucesso!");
        return "redirect:/customers";
    }

    @PostMapping("/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cliente excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Não é possível excluir este cliente pois ele possui ordens de serviço vinculadas.");
        }
        return "redirect:/customers";
    }
}
