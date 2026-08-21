package com.email.writer.service;

import com.email.writer.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String apiKey;
    private final String modelName;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApiKey,
                                 @Value("${gemini.model.name:gemini-3.6-flash}") String modelName) {
        this.apiKey = geminiApiKey;
        this.modelName = modelName;
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                       Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        String response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                       .path("/v1beta/models/{model}:generateContent")
                       .build(modelName))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            if (response == null || response.isBlank()) {
                throw new IllegalStateException("Empty response received from Gemini API");
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response);

            JsonNode candidateNode = rootNode.path("candidates").get(0);
            if (candidateNode == null || candidateNode.isMissingNode()) {
                throw new IllegalStateException("No candidates returned from Gemini API: " + response);
            }

            return candidateNode.path("content")
                   .path("parts")
                   .get(0)
                   .path("text")
                   .asText();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse response from Gemini API", ex);
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Generate a professional email reply for the following email:");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            promptBuilder.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        promptBuilder.append(" Original Email:\n").append(emailRequest.getEmailContent());
        return promptBuilder.toString();
    }
}
