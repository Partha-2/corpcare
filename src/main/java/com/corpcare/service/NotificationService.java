package com.corpcare.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.corpcare.entity.Appointment;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Value("${twilio.account.sid}")
    private String twilioSid;

    @Value("${twilio.auth.token}")
    private String twilioToken;

    @Value("${twilio.whatsapp.from}")
    private String whatsappFrom;

    @Value("${bolna.api.key}")
    private String bolnaApiKey;

    @Value("${bolna.api.url}")
    private String bolnaApiUrl;

    @Value("${bolna.agent.id}")
    private String bolnaAgentId;

    @PostConstruct
    public void init() {
        if (isTwilioConfigured()) {
            Twilio.init(twilioSid, twilioToken);
            log.info("Twilio initialized for WhatsApp");
        } else {
            log.warn("Twilio not configured — set TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN env vars for WhatsApp");
        }
        if (!isBolnaConfigured()) {
            log.warn("Bolna not configured — set BOLNA_API_KEY env var for voice calls");
        }
    }

    private boolean isTwilioConfigured() {
        return notBlank(twilioSid) && notBlank(twilioToken) && notBlank(whatsappFrom);
    }

    private boolean isBolnaConfigured() {
        return notBlank(bolnaApiKey) && !"YOUR_BOLNA_API_KEY".equals(bolnaApiKey) && notBlank(bolnaAgentId);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public void notifyAppointmentBooked(Appointment appointment) {
        var employee = appointment.getEmployee();
        var slot = appointment.getSlot();
        var hospital = slot.getHospital();

        String employeeName = employee.getFullName();
        String employeePhone = employee.getPhone();
        String hospitalName = hospital.getHospitalName();
        String hospitalCity = hospital.getCity();
        String slotDate = slot.getSlotDate().toString();
        String shiftLabel = switch (slot.getShiftType().name()) {
            case "MORNING_8_TO_4" -> "Morning (8AM - 4PM)";
            case "EVENING_4_TO_12" -> "Evening (4PM - 12AM)";
            case "NIGHT_12_TO_8" -> "Night (12AM - 8AM)";
            default -> slot.getShiftType().name();
        };

        log.info("=== APPOINTMENT BOOKED ===");
        log.info("Employee: {} ({})", employeeName, employee.getEmail());
        log.info("Hospital: {} - {}", hospitalName, hospitalCity);
        log.info("Slot: {} - {}", slotDate, shiftLabel);
        log.info("Phone: {}", employeePhone != null ? employeePhone : "NOT PROVIDED");
        log.info("==========================");

        if (employeePhone == null) {
            log.warn("No phone number for employee {} — skipping notifications", employeeName);
            return;
        }

        String formattedPhone = employeePhone.startsWith("+") ? employeePhone : "+" + employeePhone;

        triggerBolnaCall(formattedPhone, employeeName, hospitalName, hospitalCity, slotDate, shiftLabel);
        sendWhatsApp(formattedPhone, employeeName, hospitalName, hospitalCity, slotDate, shiftLabel);
    }

    private void triggerBolnaCall(String phone, String name, String hospital, String city, String date, String shift) {
        if (!isBolnaConfigured()) {
            log.warn("Bolna not configured — would call {} with appointment details", phone);
            return;
        }
        try {
            String promptText = "Your name is Nova. You are a health appointment confirmation assistant for CorpCare. "
                    + "IMPORTANT RULES: Do NOT try to repeat or pronounce the employee's name. Just say Hi there or Hello to greet. Speak slowly and clearly. Always wait 3 seconds after asking a question. Never hang up until employee says bye or confirms. "
                    + "EXACT SCRIPT TO FOLLOW: "
                    + "Step 1 GREET: Say Hello! Am I speaking with the CorpCare employee? Wait for any response like yes, hello, speaking, who is this. "
                    + "Step 2 INTRODUCE: Say Hi! I am Nova calling from CorpCare regarding your health appointment booking. "
                    + "Step 3 CONFIRM DETAILS: Say Your appointment has been successfully booked at " + hospital + " on " + date + ". Your time slot is " + shift + ". Purpose of visit is routine health checkup. "
                    + "Step 4 ASK CONFIRMATION: Say Can you confirm you received these details? Wait for yes or okay or got it. "
                    + "Step 5 CLOSE: Say Great! Please carry your Employee ID card and arrive 10 minutes before your slot. Thank you and have a great day. Goodbye! Then end the call. "
                    + "HANDLE THESE SITUATIONS: If employee says cannot hear you, repeat Step 3 slowly. If employee says wrong number or who is this, say I am calling from CorpCare regarding a health appointment. Is this the right number? If they say no, say Sorry for the trouble, goodbye. If no response for 5 seconds, say Hello, are you there? If still no response, say I will call back later. Goodbye. If employee asks to reschedule, say Please contact CorpCare support to reschedule your appointment. Thank you. Goodbye. "
                    + "NEVER hang up mid sentence. NEVER hang up without completing Step 5. ALWAYS wait for response before moving to next step.";

            String body = "{"
                    + "\"agent_id\": \"" + jsonEscape(bolnaAgentId) + "\","
                    + "\"recipient_phone_number\": \"" + jsonEscape(phone) + "\","
                    + "\"prompt\": \"" + jsonEscape(promptText) + "\","
                    + "\"user_data\": {"
                    + "\"name\": \"" + jsonEscape(name) + "\","
                    + "\"hospital\": \"" + jsonEscape(hospital) + "\","
                    + "\"city\": \"" + jsonEscape(city) + "\","
                    + "\"date\": \"" + jsonEscape(date) + "\","
                    + "\"shift\": \"" + jsonEscape(shift) + "\""
                    + "}"
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(bolnaApiUrl))
                    .header("Authorization", "Bearer " + bolnaApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            log.info("Bolna call triggered: {} -> {}", phone, response);
        } catch (Exception e) {
            log.error("Bolna call failed for {}: {}", phone, e.getMessage());
        }
    }

    private String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void sendWhatsApp(String to, String name, String hospital, String city, String date, String shift) {
        if (!isTwilioConfigured()) {
            log.warn("Twilio not configured — would send WhatsApp to {}", to);
            return;
        }
        try {
            String from = "whatsapp:" + whatsappFrom;

            String body = "✅ *Appointment Confirmed - CorpCare*\n\n"
                    + "Hi " + name + ",\n\n"
                    + "Your health checkup appointment has been booked successfully.\n\n"
                    + "🏥 *Hospital:* " + hospital + "\n"
                    + "📍 *Location:* " + city + "\n"
                    + "📅 *Date:* " + date + "\n"
                    + "⏰ *Shift:* " + shift + "\n\n"
                    + "Please arrive 15 minutes early.\n"
                    + "For any changes, contact your company HR.\n\n"
                    + "— CorpCare Team";

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + to),
                    new PhoneNumber(from),
                    body
            ).create();

            log.info("WhatsApp sent: SID={} to={}", message.getSid(), to);
        } catch (Exception e) {
            log.error("WhatsApp failed for {}: {}", to, e.getMessage());
        }
    }
}