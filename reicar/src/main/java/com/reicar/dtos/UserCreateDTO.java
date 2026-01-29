package com.reicar.dtos;

import com.reicar.entities.Customer;
import com.reicar.entities.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
    @NotBlank(message = "Nome de usuário é obrigatório")
    String username,

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
    String password,

    @NotNull(message = "Perfil é obrigatório")
    Role role,

    Long customerId
) {
    public UserCreateDTO() {
        this(null, null, null, null);
    }
}
