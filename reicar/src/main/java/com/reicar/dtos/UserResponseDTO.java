package com.reicar.dtos;

import com.reicar.entities.User;
import com.reicar.entities.enums.Role;

public record UserResponseDTO(
    Long id,
    String username,
    Role role,
    boolean enabled,
    String customerName,
    Long customerId
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.isEnabled(),
            user.getCustomer() != null ? user.getCustomer().getName() : null,
            user.getCustomer() != null ? user.getCustomer().getId() : null
        );
    }
}
