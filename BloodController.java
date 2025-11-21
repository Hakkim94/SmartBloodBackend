package com.example.demo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BloodController {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private DonorService donorService;

    @Autowired
    private SmsService smsService;

    private final List<Donor> lastAlertedDonors = new ArrayList<>();

    // Register donor
    @PostMapping("/donors")
    public ResponseEntity<?> addDonor(@RequestBody Donor donor) {
        try {
            donorRepository.save(donor);
            return ResponseEntity.ok("Donor saved");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving donor: " + e.getMessage());
        }
    }

    // Simple nearest donors
    @GetMapping("/nearest-donors")
    public ResponseEntity<?> nearestDonors(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") int k) {

        List<Donor> nearest = donorService.findNearestDonors(lat, lon, k);
        return ResponseEntity.ok(nearest);
    }

    // Old-style alert by bloodType + location
    @PostMapping("/alert/{bloodType}")
    public ResponseEntity<?> alertDonors(
            @PathVariable String bloodType,
            @RequestParam String location,
            @RequestParam String city,
            @RequestParam String message) {

        List<Donor> donors = donorRepository.findByBloodTypeAndLocationAndCity(bloodType, location, city);

        for (Donor donor : donors) {
            try {
                smsService.sendSms(donor.getPhoneNumber(), message);
            } catch (Exception ex) {
                System.err.println("SMS failed for " + donor.getPhoneNumber() + ": " + ex.getMessage());
            }
        }

        return ResponseEntity.ok("Alerts sent to donors with blood type: " + bloodType);
    }

    // ** FINAL request-blood WITHOUT units logic **
//    @PostMapping("/request-blood")
//    public ResponseEntity<?> requestBlood(@RequestBody RecipientRequest req) {
//
//        if (req == null || req.getBloodType() == null || req.getLocation() == null) {
//            return ResponseEntity.badRequest().body("Invalid request: bloodType and location required.");
//        }
//
//        List<Donor> bestDonors = donorService.findBestDonors(
//                req.getLatitude(),
//                req.getLongitude(),
//                req.getBloodType(),
//                5  // K nearest donors
//        );
//
//        if (bestDonors.isEmpty()) {
//            return ResponseEntity.ok("No matching donors available nearby.");
//        }
//
//        lastAlertedDonors.clear();
//        lastAlertedDonors.addAll(bestDonors);
//
//        for (Donor donor : bestDonors) {
//
//            String msg = "URGENT BLOOD REQUEST!\n" +
//                    "Blood: " + req.getBloodType() + "\n" +
//                    "Location: " + req.getLocation() + "\n" +
//                    "A nearby patient needs help. Please donate if possible.";
//
//            try {
//                smsService.sendSms(donor.getPhoneNumber(), msg);
//            } catch (Exception ex) {
//                System.err.println("Failed to send SMS to " + donor.getPhoneNumber() + " : " + ex.getMessage());
//            }
//        }
//
//        return ResponseEntity.ok(bestDonors.size() + " donor(s) alerted!");
//    }
    @PostMapping("/request-blood")
    public ResponseEntity<?> requestBlood(@RequestBody RecipientRequest req) {

        List<Donor> best = donorService.findBestDonors(
                req.getLatitude(),
                req.getLongitude(),
                req.getBloodType(),
                5 // number of donors to alert
        );

        if(best.isEmpty())
            return ResponseEntity.ok(best);

        lastAlertedDonors.clear();
        lastAlertedDonors.addAll(best);

        // SEND SMS TO DONORS
        for(Donor d : best){
            try {
                String msg = 
                    "🚨 URGENT BLOOD REQUEST 🚨\n" +
                    "Blood Group: " + req.getBloodType() + "\n" +
                    "Location: " + req.getLocation() + "\n" +
                    "You have been selected as a nearby donor.\n" +
                    "Please help if possible ❤️";

                smsService.sendSms(d.getPhoneNumber(), msg);

            } catch(Exception e){
                System.out.println("SMS failed: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(best);
    }

    // Dashboard endpoint: show last alerted donors
    @GetMapping("/donor-dashboard/last-request")
    public ResponseEntity<?> getLastAlertedDonors() {
        if (lastAlertedDonors.isEmpty()) {
            return ResponseEntity.ok("No recent blood request.");
        }
        return ResponseEntity.ok(lastAlertedDonors);
    }
}
