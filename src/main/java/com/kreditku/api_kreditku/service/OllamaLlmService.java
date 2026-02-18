package com.kreditku.api_kreditku.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaLlmService implements LlmService {
    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Autowired
    private CardKnowledgeService cardKnowledgeService;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create(ollamaBaseUrl);
        System.out.println("✅ LLM Provider: Ollama (" + ollamaModel + ")");
    }

    @Override
    public String getRecommendation(String expensesText) {
        String cardContext = cardKnowledgeService.getCardsAsText();
        String prompt = buildRecommendationPrompt(expensesText, cardContext);
        return callOllama(prompt);
    }

    @Override
    public String chat(String userMessage) {
        String cardContext = cardKnowledgeService.getCardsAsText();
        String prompt = buildChatPrompt(userMessage, cardContext);
        return callOllama(prompt);
    }

    @Override
    public boolean isHealthy() {
        try {
            Map<String, Object> tags = webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }) // ← changed
                    .block();
            return tags != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "ollama:" + ollamaModel;
    }

    private String callOllama(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content",
                                "You are a helpful Malaysian credit card advisor. Only recommend cards from the provided list. Be concise and specific."),
                        Map.of("role", "user", "content", prompt)),
                "stream", false);

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/chat")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }) // ← changed
                    .block();

            if (response != null && response.containsKey("message")) {
                Map<String, Object> message = (Map<String, Object>) response.get("message");
                return (String) message.get("content");
            }

            return "No response from Ollama.";

        } catch (Exception e) {
            return "Error connecting to Ollama. Run: ollama serve";
        }
    }

    private String buildRecommendationPrompt(String expensesText, String cardContext) {
        return """
                Based on the monthly expenses below, recommend the top 3 credit cards from the list.
                For each card explain specifically which expense category it helps most.

                === Monthly Expenses ===
                %s

                === Available Credit Cards ===
                %s
                """.formatted(expensesText, cardContext);
    }

    private String buildChatPrompt(String userMessage, String cardContext) {
        return """
                Answer the following question about Malaysian credit cards.
                Only reference cards from the list provided.

                === Question ===
                %s

                === Available Credit Cards ===
                %s
                """.formatted(userMessage, cardContext);
    }
}
