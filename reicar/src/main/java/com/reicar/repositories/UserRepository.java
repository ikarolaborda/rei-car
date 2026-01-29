package com.reicar.repositories;

import com.reicar.entities.User;
import com.reicar.entities.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.customer WHERE u.username = :username")
    Optional<User> findByUsernameWithCustomer(String username);
}
