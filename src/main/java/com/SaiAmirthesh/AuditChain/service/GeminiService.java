package com.SaiAmirthesh.AuditChain.service;

import com.SaiAmirthesh.AuditChain.entity.Alert;
import com.SaiAmirthesh.AuditChain.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private AlertRepository alertRepository;

    public String generateSummary() {
        List<Alert> alerts = alertRepository.findAll();
        if (alerts.isEmpty()) {
            return "No alerts found. The system is secure.";
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are an expert financial auditor AI. Only narrate what is the issue from the alerts table or audit log chain broken response. Be extremely concise. Do not waste tokens generating other stuffs.\\n\\n");
        for (Alert alert : alerts) {
            promptBuilder.append("- Alert [").append(alert.getStatus()).append("]: ").append(alert.getAlertMessage()).append("\\n");
        }

        String prompt = promptBuilder.toString();
        String jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt.replace("\"", "\\\"") + "\"}]}]}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String responseBody = response.body();
            String textMarker = "\"text\": \"";
            int start = responseBody.indexOf(textMarker);
            if (start != -1) {
                start += textMarker.length();
                int end = responseBody.indexOf("\"", start);
                String extracted = responseBody.substring(start, end);
                return extracted.replace("\\n", "\n").replace("\\\"", "\"");
            }
            return "Failed to parse AI response: " + responseBody;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}
