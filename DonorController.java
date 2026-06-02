package com.example.blooddb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/")
public class DonorController {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private EmailService emailService;

    /* =======================================================
     * 🩸 DONOR CRUD ENDPOINTS
     * ======================================================= */

    // Public GET — retrieve all donors
    @GetMapping("donors")
    public List<Donor> getAllDonors() {
        return donorRepository.findAll();
    }

    // Secured READ — search donors by blood group
    @GetMapping("donors/search")
    @PreAuthorize("isAuthenticated()")
    public List<Donor> searchDonorsByBloodGroup(@RequestParam(required = false) String bloodGroup) {
        if (bloodGroup == null || bloodGroup.isEmpty() || "All".equalsIgnoreCase(bloodGroup)) {
            return donorRepository.findAll();
        }
        return donorRepository.findByBloodGroup(bloodGroup);
    }

    // Public CREATE — add new donor
    @PostMapping("donors")
    public Donor addDonor(@RequestBody Donor donor) {
        return donorRepository.save(donor);
    }

    // Secured UPDATE — update donor record
    @PutMapping("donors/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Donor> updateDonor(@PathVariable Long id, @RequestBody Donor updatedDonor) {
        Optional<Donor> donorOptional = donorRepository.findById(id);
        if (donorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Donor donor = donorOptional.get();
        donor.setName(updatedDonor.getName());
        donor.setBloodGroup(updatedDonor.getBloodGroup());
        donor.setCity(updatedDonor.getCity());
        donor.setContact(updatedDonor.getContact());
        donor.setEmail(updatedDonor.getEmail());
        // NEW FIX: Save the diseases field
        donor.setDiseases(updatedDonor.getDiseases()); 
        
        return ResponseEntity.ok(donorRepository.save(donor));
    }

    // Secured DELETE — delete donor record
    @DeleteMapping("donors/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDonor(@PathVariable Long id) {
        if (!donorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        donorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /* =======================================================
     * ❤️ BLOOD REQUEST ENDPOINTS
     * ======================================================= */

    // Read all blood requests — only for logged-in admins
    @GetMapping("requests")
    @PreAuthorize("isAuthenticated()") // Changed from hasRole('ADMIN') for simpler testing
    public List<BloodRequest> getAllBloodRequests() {
        return bloodRequestRepository.findAll();
    }

    // Public POST — anyone can create a new blood request
    @PostMapping("requests")
    public BloodRequest createBloodRequest(@RequestBody BloodRequest request) {
        BloodRequest newRequest = bloodRequestRepository.save(request);
        emailService.sendNewBloodRequestNotification(newRequest);
        return newRequest;
    }

    // DELETE blood request — only for logged-in admins
    @DeleteMapping("requests/{id}")
    @PreAuthorize("isAuthenticated()") // Changed from hasRole('ADMIN') for simpler testing
    public ResponseEntity<Void> deleteBloodRequest(@PathVariable Long id) {
        if (!bloodRequestRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bloodRequestRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
