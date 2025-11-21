package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonorRepository extends JpaRepository<Donor, Long> {

    // Find donors by blood type
    List<Donor> findByBloodType(String bloodType);

    // Find donors by blood type and human-readable location and city
    List<Donor> findByBloodTypeAndLocationAndCity(String bloodType, String location, String city);
}
