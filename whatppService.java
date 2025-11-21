
package com.example.demo;

import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class whatppService {

    private static final String ACCOUNT_SID = "AC49156394a39308700662b659b6b2d848";
    private static final String AUTH_TOKEN = "110c8f5d5840481d111f6374fe3a81b4";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendWhatsApp(String toNumber, String msg) {
        try {
            Message.creator(
                new PhoneNumber("whatsapp:" + toNumber),
                new PhoneNumber("whatsapp:+14155238886"),
                msg
            ).create();
        } catch (Exception e) {
            System.out.println("WhatsApp failed: " + e.getMessage());
        }
    }
}
