package com.corpcare.pdfsplit.service.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmClassifierService {

    private static final Logger log = LoggerFactory.getLogger(LlmClassifierService.class);
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    public boolean isAvailable() {
        return groqApiKey != null && !groqApiKey.isEmpty() && !groqApiKey.startsWith("${");
    }

    public LlmResult classifyText(String pageText) {
        if (!isAvailable() || pageText == null || pageText.isBlank()) return null;

        String prompt = """
            You are a medical document classifier. Analyze the text below from one page of a medical PDF and classify it into exactly one category.

            Categories and their descriptions:
            - LAB: Laboratory reports — blood tests, urine tests, pathology, CBC, lipid profile, glucose, hemoglobin, thyroid, etc.
            - EYE: Ophthalmology reports — vision tests, retina exams, cataract, glaucoma, intraocular pressure, etc.
            - CHEST: Chest / cardiopulmonary reports — lung, cardiac, X-ray, ECG, PFT, echocardiogram, etc.

            Rules:
            - Return ONLY a JSON object with fields: "type", "confidence"
            - "type" must be one of: LAB, EYE, CHEST
            - "confidence" must be one of: HIGH, MEDIUM, LOW
            - HIGH = clear strong evidence, MEDIUM = moderate evidence, LOW = weak or ambiguous

            Text: """ + truncate(pageText, 1500) + "\n\nJSON:";

        return callLlm(prompt);
    }

    public LlmResult classifyImage(String ocrText) {
        if (!isAvailable() || ocrText == null || ocrText.isBlank()) return null;

        String prompt = """
            You are a medical image classifier. Analyze the OCR text below from a medical image page and classify it into exactly one category.

            Categories and their descriptions:
            - ECG: Electrocardiogram — heart rhythm strips, QRS, ST segment, leads, etc.
            - XRAY: X-ray images — chest X-ray, radiograph, lung opacity, etc.
            - MRI: MRI scans — magnetic resonance imaging, brain, spine, etc.
            - USG: Ultrasound / sonography — abdomen, pelvis, thyroid, etc.
            - PRESCRIPTION: Prescriptions — medication list, dosage, tablet/capsule names, etc.
            - LAB: Laboratory reports — blood/urine test results printed or scanned
            - EYE: Ophthalmology reports — vision tests, retina
            - CHEST: Chest / cardiopulmonary — X-ray, ECG, PFT, cardiac reports
            - UNKNOWN: Cannot determine

            Rules:
            - Return ONLY a JSON object with fields: "type", "confidence", and optionally "values" (object mapping field names to extracted values)
            - "type" must be one of: ECG, XRAY, MRI, USG, PRESCRIPTION, LAB, EYE, CHEST, UNKNOWN
            - "confidence" must be one of: HIGH, MEDIUM, LOW
            - "values" should contain any medically relevant measurements found (heart rate, blood pressure, lab values, etc.)

            Text: """ + truncate(ocrText, 1500) + "\n\nJSON:";

        return callLlm(prompt);
    }

    private LlmResult callLlm(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", groqModel,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.1,
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

            String content = extractContent(response.getBody());
            if (content == null) return null;

            return parseResponse(content);
        } catch (Exception e) {
            log.warn("LLM classifier call failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractContent(Map<?, ?> body) {
        if (body == null) return null;
        try {
            List<?> choices = (List<?>) body.get("choices");
            if (choices == null || choices.isEmpty()) return null;
            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> msg = (Map<?, ?>) first.get("message");
            return msg != null ? (String) msg.get("content") : null;
        } catch (Exception e) {
            return null;
        }
    }

    private LlmResult parseResponse(String content) {
        try {
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
                if (content.endsWith("```"))
                    content = content.substring(0, content.length() - 3);
            } else if (content.startsWith("```")) {
                content = content.substring(3);
                if (content.endsWith("```"))
                    content = content.substring(0, content.length() - 3);
            }
            content = content.trim();

            Map<String, Object> map = mapper.readValue(content, new TypeReference<Map<String, Object>>() {});

            LlmResult result = new LlmResult();
            result.type = (String) map.getOrDefault("type", "UNKNOWN");
            result.confidence = (String) map.getOrDefault("confidence", "LOW");

            Object valuesObj = map.get("values");
            if (valuesObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> values = (Map<String, String>) valuesObj;
                result.values = values;
            }

            return result;
        } catch (Exception e) {
            log.warn("Failed to parse LLM response: {}", content);
            return null;
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...[truncated]";
    }

    public static class LlmResult {
        private String type;
        private String confidence;
        private Map<String, String> values;

        public String getType() { return type; }
        public String getConfidence() { return confidence; }
        public Map<String, String> getValues() { return values; }

        public void setType(String type) { this.type = type; }
        public void setConfidence(String confidence) { this.confidence = confidence; }
        public void setValues(Map<String, String> values) { this.values = values; }
    }
}
