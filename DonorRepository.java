package com.example.blooddb;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    // Existing method for general filtering/search on dashboard
    List<Donor> findByBloodGroup(String bloodGroup);

    // Find donors matching BOTH blood type and city, used for email matching
    List<Donor> findByBloodGroupAndCity(String bloodGroup, String city);
    
    // NEW METHOD: Find donors by a specific disease (for future admin filtering)
    List<Donor> findByDiseasesContaining(String disease);
}
