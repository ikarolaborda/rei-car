package com.reicar.controllers;

import com.reicar.dtos.UserCreateDTO;
import com.reicar.dtos.UserResponseDTO;
import com.reicar.dtos.UserUpdateDTO;
import com.reicar.entities.enums.Role;
import com.reicar.repositories.CustomerRepository;
import com.reicar.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;
    private final CustomerRepository customerRepository;

    @GetMapping
    public String listUsers(Model model) {
        var users = userService.findAll().stream()
            .map(UserResponseDTO::from)
            .toList();
        model.addAttribute("users", users);
        return "users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new UserCreateDTO());
        model.addAttribute("roles", Role.values());
        model.addAttribute("customers", customerRepository.findAll());
        return "users/form";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("user") UserCreateDTO dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("customers", customerRepository.findAll());
            return "users/form";
        }

        try {
            userService.createUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário criado com sucesso!");
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Role.values());
            model.addAttribute("customers", customerRepository.findAll());
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        var user = userService.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        model.addAttribute("user", UserResponseDTO.from(user));
        model.addAttribute("roles", Role.values());
        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("editing", true);
        return "users/form";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute UserUpdateDTO dto,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário atualizado com sucesso!");
            return "redirect:/users";
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuário excluído com sucesso!");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        var user = userService.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        userService.updateUser(id, new UserUpdateDTO(null, null, !user.isEnabled(), null));
        redirectAttributes.addFlashAttribute("successMessage",
            user.isEnabled() ? "Usuário desativado!" : "Usuário ativado!");
        return "redirect:/users";
    }
}
