package com.corpcare.controller;

import com.corpcare.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model}")
    private String groqModel;

    private final RestTemplate rest = new RestTemplate();

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        String keyPreview = groqApiKey != null && groqApiKey.startsWith("gsk_")
            ? groqApiKey.substring(0, 10) + "..."
            : "not set";
        return ResponseEntity.ok(ApiResponse.success("ok", Map.of(
            "key", keyPreview,
            "model", groqModel,
            "configured", groqApiKey != null && !groqApiKey.isEmpty() && !groqApiKey.startsWith("${")
        )));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> chat(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Message is required"));
        }

        if (groqApiKey == null || groqApiKey.isEmpty() || groqApiKey.startsWith("${")) {
            log.warn("Groq API key not configured");
            return ResponseEntity.ok(ApiResponse.success("ok",
                "The chatbot is not configured yet. Ask your admin to set the GROQ_API_KEY environment variable."));
        }

        String prompt = """
            You are CorpCare Assistant for a B2B corporate employee health management platform.
            Answer concisely and helpfully.
            
            CorpCare features:
            - 4 portals: Admin (password-protected), Client (corporate HR), Hospital (partner), Employee (self-service)
            - Employee login: email + employee code
            - Booking flow: employee picks hospital → sees available slots → books → WhatsApp (Twilio) + voice call (Bolna.ai)
            - Admin manages clients, hospitals
            - Client onboard employees, record vitals, book
            - Hospitals create slots with shifts: Morning 8-4, Evening 4-12, Night 12-8
            - One slot = one booking. One employee = one active booking. Max 100 employees per client.
            - Vitals: height, weight, blood pressure, blood sugar, blood group
            
            User question: """ + message;

        try {
            Map<String, Object> requestBody = Map.of(
                "model", groqModel,
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
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String respBody = e.getResponseBodyAsString();
            log.error("Groq API error {}: {}", e.getStatusCode(), respBody);
            return ResponseEntity.ok(ApiResponse.success("ok",
                "⚠️ Groq API error (" + e.getStatusCode() + "). The model \"" + groqModel + "\" may not be available. Try setting a different model via GROQ_MODEL env var."));
        } catch (Exception e) {
            log.error("Groq chat failed", e);
            return ResponseEntity.ok(ApiResponse.success("ok",
                "Sorry, I'm having trouble connecting. Please try again later."));
        }
    }
}
