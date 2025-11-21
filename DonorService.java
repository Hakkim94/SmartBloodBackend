package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DonorService {

    @Autowired
    private DonorRepository donorRepository;

    // Simple nearest donors search (no filters)
    public List<Donor> findNearestDonors(double lat, double lon, int k) {
        List<Donor> donors = donorRepository.findAll();
        return donors.stream()
                .sorted(Comparator.comparingDouble(
                        d -> Haversine.distance(lat, lon, d.getLatitude(), d.getLongitude())
                ))
                .limit(k)
                .collect(Collectors.toList());
    }

    // Find nearest compatible donors by blood type only (NO units)
    public List<Donor> findBestDonors(double lat, double lon, String bloodType, int k) {
        List<Donor> donors = donorRepository.findByBloodType(bloodType);
        return donors.stream()
                .sorted(Comparator.comparingDouble(
                        d -> Haversine.distance(lat, lon, d.getLatitude(), d.getLongitude())
                ))
                .limit(k)
                .collect(Collectors.toList());
    }
}
