//package com.example.demo;
//
//import java.util.ArrayList;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/donor")
//@CrossOrigin(origins = "*")
//public class DonorController {
//
//    @Autowired
//    private DonorRepository donorRepository;
//
//    @Autowired
//    private DonorService donorService;
//
//    @Autowired
//    private SmsService smsService;
//
//    private List<Donor> lastAlertedDonors = new ArrayList<>();
//
//    @PostMapping("/save")
//    public ResponseEntity<?> save(@RequestBody Donor donor){
//        try{
//            donorRepository.save(donor);
//            return ResponseEntity.ok("Donor Registered Successfully!");
//        }catch(Exception e){
//            return ResponseEntity.status(500).body("Error: "+e.getMessage());
//        }
//    }
//
//    @GetMapping("/best")
//    public ResponseEntity<?> bestDonors(@RequestParam double lat,
//                                        @RequestParam double lon,
//                                        @RequestParam String bloodType,
//                                        @RequestParam(defaultValue="5") int k){
//
//        List<Donor> best = donorService.findBestDonors(lat, lon, bloodType, k);
//
//        if(best.isEmpty())
//            return ResponseEntity.ok("No compatible donors found nearby.");
//
//        return ResponseEntity.ok(best);
//    }
//
//    @PostMapping("/request-blood")
//    public ResponseEntity<?> requestBlood(@RequestBody RecipientRequest req){
//
//        List<Donor> best = donorService.findBestDonors(
//                req.getLatitude(),
//                req.getLongitude(),
//                req.getBloodType(),
//                5   // K-NEAREST DONORS ONLY
//        );
//
//        if(best.isEmpty())
//            return ResponseEntity.ok("No matching donors available nearby.");
//
//        lastAlertedDonors.clear();
//        lastAlertedDonors.addAll(best);
//
//        for(Donor d: best){
//            String msg = "URGENT BLOOD REQUEST\n" +
//                         "Blood: "+req.getBloodType()+"\n" +
//                         "Location: "+req.getLocation()+"\n" +
//                         "A nearby patient needs help.\n" +
//                         "Please donate if possible.";
//            smsService.sendSms(d.getPhoneNumber(), msg);
//        }
//
//        return ResponseEntity.ok(best.size()+" compatible donor(s) alerted!");
//    }
//
//    @GetMapping("/dashboard/requests")
//    public ResponseEntity<?> dashboard(){
//        if(lastAlertedDonors.isEmpty())
//            return ResponseEntity.ok("No recent blood request.");
//        return ResponseEntity.ok(lastAlertedDonors);
//    }
//    
//    
//}
package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donor")
@CrossOrigin(origins = "*")
public class DonorController {

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private DonorService donorService;
    
    @Autowired
    private whatppService WhatsappService;

    @Autowired
    private SmsService smsService;

    private List<Donor> lastAlertedDonors = new ArrayList<>();

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody Donor donor){
        try{
            donorRepository.save(donor);
            return ResponseEntity.ok("Donor Registered Successfully!");
        }catch(Exception e){
            return ResponseEntity.status(500).body("Error: "+e.getMessage());
        }
    }

    @GetMapping("/best")
    public ResponseEntity<?> bestDonors(@RequestParam double lat,
                                        @RequestParam double lon,
                                        @RequestParam String bloodType,
                                        @RequestParam(defaultValue="5") int k){

        List<Donor> best = donorService.findBestDonors(lat, lon, bloodType, k);

        return ResponseEntity.ok(best);
    }

    @PostMapping("/request-blood")
    public ResponseEntity<?> requestBlood(@RequestBody RecipientRequest req){

        List<Donor> best = donorService.findBestDonors(
                req.getLatitude(),
                req.getLongitude(),
                req.getBloodType(),
                5
        );

        if(best.isEmpty())
            return ResponseEntity.ok("No matching donors available nearby.");

        lastAlertedDonors.clear();
        lastAlertedDonors.addAll(best);

        String msg =
                "🚨 URGENT BLOOD REQUEST\n" +
                "Blood: " + req.getBloodType() + "\n" +
                "Location: " + req.getLocation() + "\n" +
                "A patient nearby needs help.\n" +
                "Please donate if you can.";

        for (Donor donor : best) {

            // send SMS
            try { smsService.sendSms(donor.getPhoneNumber(), msg); }
            catch (Exception ex) { System.out.println("SMS failed: " + ex.getMessage()); }

            // send WhatsApp
            try { WhatsappService.sendWhatsApp(donor.getPhoneNumber(), msg); }
            catch (Exception ex) { System.out.println("WA failed: " + ex.getMessage()); }
        }

        return ResponseEntity.ok(best); // send list of donors to frontend
    }

    @GetMapping("/dashboard/requests")
    public ResponseEntity<?> dashboard(){
        return ResponseEntity.ok(lastAlertedDonors);
    }

}
