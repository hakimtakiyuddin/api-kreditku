package com.kreditku.api_kreditku.service.llm.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.kreditku.api_kreditku.service.knowledge.CardKnowledgeService;
import com.kreditku.api_kreditku.service.llm.LlmService;

import jakarta.annotation.PostConstruct;

@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
public class OpenAiLlmService implements LlmService {
    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Autowired
    private CardKnowledgeService cardKnowledgeService;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.create("https://api.openai.com");
        System.out.println("✅ LLM Provider: OpenAI (" + model + ")");
    }

    @Override
    public String getRecommendation(String expensesText) {
        String cardContext = cardKnowledgeService.getCardsAsText();
        String prompt = buildRecommendationPrompt(expensesText, cardContext);
        return callOpenAi(prompt);
    }

    @Override
    public String chat(String userMessage) {
        String cardContext = cardKnowledgeService.getCardsAsText();
        String prompt = buildChatPrompt(userMessage, cardContext);
        return callOpenAi(prompt);
    }

    @Override
    public boolean isHealthy() {
        try {
            Map<String, Object> response = webClient.get()
                .uri("/v1/models")
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "openai:" + model;
    }

    @SuppressWarnings("unchecked")
    private String callOpenAi(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of(
                    "role", "system",
                    "content", "You are a helpful Malaysian credit card advisor. Only recommend cards from the provided list. Be concise and specific."
                ),
                Map.of("role", "user", "content", prompt)
            ),
            "temperature", 0.7,
            "max_tokens", 500
        );

        try {
            Map<String, Object> response = webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return "No response from OpenAI.";

        } catch (Exception e) {
            return "Error connecting to OpenAI: " + e.getMessage();
        }
    }

    private String buildRecommendationPrompt(String expensesText, String cardContext) {
        return String.format(
            "Based on the monthly expenses below, recommend the top 3 credit cards from the list. " +
            "For each card, explain specifically which expense category it helps most.\n\n" +
            "=== Monthly Expenses ===\n%s\n\n" +
            "=== Available Credit Cards ===\n%s",
            expensesText, cardContext
        );
    }

    private String buildChatPrompt(String userMessage, String cardContext) {
        return String.format(
            "Answer the following question about Malaysian credit cards. " +
            "Only reference cards from the list provided.\n\n" +
            "=== Question ===\n%s\n\n" +
            "=== Available Credit Cards ===\n%s",
            userMessage, cardContext
        );
    }
}
