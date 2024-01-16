package com.example.emissions.repository;

import com.example.emissions.model.Emission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmissionRepository extends JpaRepository<Emission, Long> {
    // You can add custom query methods if needed
}
