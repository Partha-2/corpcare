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
            String promptText = "You are a confirmation assistant for CorpCare calling " + name + ". "
                    + "Follow this exact flow and DO NOT skip any step. "
                    + "Step 1: Say hello and ask if you are speaking with " + name + ". Wait for their reply. "
                    + "Step 2: Introduce yourself as calling from CorpCare regarding their health appointment. "
                    + "Step 3: Say their appointment is at " + hospital + " on " + date + " shift " + shift + " for a routine health checkup. "
                    + "Step 4: Ask if the details are correct. Wait for their response. "
                    + "Step 5: If they confirm, say carry Employee ID, arrive 10 minutes early, thank them and say goodbye. "
                    + "If they want to reschedule, tell them to contact CorpCare support and say goodbye. "
                    + "IMPORTANT: Never hang up before Step 4. Always wait for the person to respond after each question.";

            String body = "{"
                    + "\"agent_id\": \"" + bolnaAgentId + "\","
                    + "\"recipient_phone_number\": \"" + phone + "\","
                    + "\"prompt\": \"" + promptText.replace("\"", "\\\"") + "\","
                    + "\"user_data\": {"
                    + "\"name\": \"" + name + "\","
                    + "\"hospital\": \"" + hospital + "\","
                    + "\"city\": \"" + city + "\","
                    + "\"date\": \"" + date + "\","
                    + "\"shift\": \"" + shift + "\""
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