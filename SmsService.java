package com.example.demo;


import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final String ACCOUNT_SID = "AC2049a74058e264050e2d43f3dc0a41b1";
    private static final String AUTH_TOKEN = "f27ccc2838a7b0eefd3b76bbed8cf8a7";
    private static final String FROM_NUMBER = "";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String to, String message) {

        // Auto-correct 10-digit Indian number
        if (to != null && to.matches("\\d{10}")) {
            to = "+91" + to;
        }

        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(FROM_NUMBER),
                    message
            ).create();
        } catch (Exception e) {
            System.out.println("Twilio SMS Error: " + e.getMessage());
            // Do NOT throw again
        }
    }

}
