package com.example.blooddb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    // Spring Data JPA automatically creates all the methods for us
    // like save(), findAll(), deleteById(), etc.
    // We can add custom search methods here later if we want.
}
