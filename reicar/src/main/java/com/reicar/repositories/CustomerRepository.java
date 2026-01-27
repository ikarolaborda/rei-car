package com.reicar.repositories;

import com.reicar.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Persquisa por nome
    List<Customer> findByNameContainingIgnoreCase(String name);
}
