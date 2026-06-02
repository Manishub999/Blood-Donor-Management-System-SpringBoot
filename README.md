# Blood Donor Management System

## Overview

Blood Donor Management System is a web-based application developed using Spring Boot and MySQL.

The system helps manage blood donors and blood requests. Users can register as donors, request blood, and view information through a simple web interface.

The project uses Spring Boot, Spring Security, JWT Authentication, JPA, Hibernate, and MySQL.

## Features

* Donor Registration
* Blood Request Management
* User Authentication
* JWT Security
* Spring Security Integration
* MySQL Database Storage
* View Donor Information
* View Blood Requests
* Responsive Web Pages

## Technologies Used

* Java 17
* Spring Boot
* Spring Security
* JWT Authentication
* Spring Data JPA
* Hibernate
* MySQL
* HTML
* CSS
* JavaScript
* Maven

## Project Structure

* AuthController.java
* DonorController.java
* Donor.java
* BloodRequest.java
* User.java
* DonorRepository.java
* BloodRequestRepository.java
* UserRepository.java
* SecurityConfig.java
* JwtTokenUtil.java
* JwtRequestFilter.java
* CustomUserDetailsService.java
* EmailService.java
* BlooddbApplication.java

## Database

The application uses MySQL as the backend database.

Main operations:

* Insert donor details
* Insert blood requests
* Retrieve donor records
* Retrieve blood request records

## Security

* Spring Security
* JWT Authentication
* User Login System
* Protected Routes

## How to Run

1. Configure MySQL database.
2. Update application.properties.
3. Run:

mvn spring-boot:run

4. Open:

http://localhost:8080

## Future Improvements

* Email Notifications
* Blood Availability Tracking
* Hospital Integration
* Mobile Application
* Advanced Search Filters

## Author

Manish Kapadi
