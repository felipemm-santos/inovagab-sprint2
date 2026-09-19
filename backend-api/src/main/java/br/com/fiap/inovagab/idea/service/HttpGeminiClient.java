package br.com.fiap.inovagab.idea.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import br.com.fiap.inovagab.shared.exception.ApiException;

@Component
@RequiredArgsConstructor
public class HttpGeminiClient implements GeminiClient {

    private final ObjectMapper mapper;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    @Override
    public String evaluate(String context) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_NOT_CONFIGURED",
                    "AI evaluation is unavailable because GEMINI_API_KEY is not configured");
        }
        try {
            Map<String, Object> schema = Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "score", Map.of("type", "integer"),
                            "priority", Map.of("type", "string", "enum", List.of("LOW", "MEDIUM", "HIGH", "CRITICAL")),
                            "strategyIds", Map.of("type", "array", "items", Map.of("type", "string")),
                            "reason", Map.of("type", "string")
                    ),
                    "required", List.of("score", "priority", "strategyIds", "reason")
            );
            var body = mapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", context)))),
                    "generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", schema)
            ));
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_QUOTA_EXCEEDED",
                        "Gemini quota has been reached");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI_PROVIDER_ERROR",
                        "Gemini returned an HTTP error");
            }
            JsonNode root = mapper.readTree(response.body());
            JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (!text.isTextual() || text.asText().isBlank()) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI_EMPTY_RESPONSE",
                        "Gemini returned an empty evaluation");
            }
            return text.asText();
        } catch (ApiException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "Gemini request was interrupted");
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "AI_UNAVAILABLE", "Gemini is unavailable or timed out");
        }


    }
}
