package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Value("${groq.api.key}")
    private String groqApiKey;

    private final RestTemplate rest = new RestTemplate();

    @PostMapping
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message is required"));
        }

        String prompt = """
            You are CorpCare Assistant, a helpful support bot for CorpCare — a B2B corporate employee health management platform.
            Answer the user's question concisely and helpfully based on what CorpCare does.
            
            CorpCare features:
            - 4 portals: Admin (password-protected), Client (corporate HR), Hospital (partner hospitals), Employee (self-service)
            - Employees log in with email + employee code
            - Booking: employee picks hospital → sees available slots → books one → gets WhatsApp (Twilio) + voice call (Bolna.ai)
            - Admins manage clients, hospitals, system-wide data
            - Clients onboard employees, record vitals, book appointments
            - Hospitals create slots with shifts (Morning 8-4, Evening 4-12, Night 12-8)
            - One slot = one booking. One employee = one active booking. Max 100 employees per client.
            - Vitals: height, weight, blood pressure, blood sugar, blood group
            
            User question: """ + message;

        try {
            Map<String, Object> requestBody = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 300
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = rest.exchange(
                "https://api.groq.com/openai/v1/chat/completions",
                HttpMethod.POST, entity, Map.class
            );

            Map bodyResp = response.getBody();
            List choices = (List) bodyResp.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map first = (Map) choices.get(0);
                Map msg = (Map) first.get("message");
                String content = (String) msg.get("content");
                return ResponseEntity.ok(ApiResponse.success("ok", content.trim()));
            }

            return ResponseEntity.ok(ApiResponse.success("ok", "I couldn't process that. Please try again."));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("ok", "Sorry, I'm having trouble connecting. Please try again later."));
        }
    }
}
