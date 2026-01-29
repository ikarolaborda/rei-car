package com.reicar.dtos;

import com.reicar.entities.enums.Role;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    String password,

    Role role,

    Boolean enabled,

    Long customerId
) {}
