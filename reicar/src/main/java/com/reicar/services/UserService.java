package com.reicar.services;

import com.reicar.dtos.UserCreateDTO;
import com.reicar.dtos.UserUpdateDTO;
import com.reicar.entities.Customer;
import com.reicar.entities.User;
import com.reicar.entities.enums.Role;
import com.reicar.repositories.CustomerRepository;
import com.reicar.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("Nome de usuário já existe");
        }

        Customer customer = null;
        if (dto.role() == Role.CUSTOMER) {
            if (dto.customerId() == null) {
                throw new IllegalArgumentException("Perfil CUSTOMER requer vínculo com um cliente");
            }
            customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        } else if (dto.customerId() != null) {
            customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        }

        User user = User.builder()
            .username(dto.username())
            .password(passwordEncoder.encode(dto.password()))
            .role(dto.role())
            .enabled(true)
            .customer(customer)
            .build();

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        if (dto.role() != null) {
            user.setRole(dto.role());

            if (dto.role() == Role.CUSTOMER && user.getCustomer() == null && dto.customerId() == null) {
                throw new IllegalArgumentException("Perfil CUSTOMER requer vínculo com um cliente");
            }
        }

        if (dto.enabled() != null) {
            user.setEnabled(dto.enabled());
        }

        if (dto.customerId() != null) {
            Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
            user.setCustomer(customer);
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }
}
