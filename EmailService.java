package com.example.blooddb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private DonorRepository donorRepository; // <-- ADDED REPOSITORY

    // We'll use the configured Gmail account as the sender
    private String fromEmail = "zurzrixgixit@gmail.com"; 
    
    /**
     * Finds matching donors and sends a notification to each one.
     * @param request The new BloodRequest object
     */
    public void sendNewBloodRequestNotification(BloodRequest request) {
        
        // 1. FIND MATCHING DONORS
        List<Donor> matchingDonors = donorRepository.findByBloodGroupAndCity(
            request.getBloodGroup(), 
            request.getCity()
        );

        if (matchingDonors.isEmpty()) {
            System.out.println("No matching donors found in " + request.getCity() + " for " + request.getBloodGroup());
            return;
        }

        System.out.println("Found " + matchingDonors.size() + " matching donors. Sending alerts.");

        // 2. SEND ALERT TO EACH MATCHING DONOR
        for (Donor donor : matchingDonors) {
             try {
                SimpleMailMessage message = new SimpleMailMessage();
                
                // Set the recipient to the DONOR's email
                message.setTo(donor.getEmail()); 
                
                // Set the sender (must be your configured Gmail)
                message.setFrom(fromEmail); 
                
                // Customize the subject line
                message.setSubject("URGENT Blood Alert: Your " + donor.getBloodGroup() + " is Needed Near " + donor.getCity());

                // Build the customized email body text
                String emailBody = "Dear " + donor.getName() + ",\n\n"
                    + "An urgent blood request has been submitted near you that matches your blood type:\n\n"
                    + "Needed Blood Group: " + request.getBloodGroup() + "\n"
                    + "Hospital: " + request.getHospitalName() + "\n"
                    + "Location: " + request.getCity() + "\n"
                    + "Contact Patient/Hospital Directly: " + request.getContactPhone() + "\n\n"
                    + "Please consider responding immediately.\n\n"
                    + "Thank you,\n"
                    + "BloodConnect Team";

                message.setText(emailBody);
                
                // Send the email
                mailSender.send(message);
                System.out.println("Successfully notified: " + donor.getEmail());

            } catch (Exception e) {
                // Log the error for this specific donor, but continue trying others
                System.err.println("Error sending email to " + donor.getEmail() + ": " + e.getMessage());
            }
        }
    }
}