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
@ConditionalOnProperty(name = "llm.provider", havingValue = "huggingface")
public class HuggingFaceLlmService implements LlmService {
    
    @Value("${huggingface.api.key}")
    private String apiToken;

    @Value("${huggingface.model}")
    private String model;

    @Autowired
    private CardKnowledgeService cardKnowledgeService;

    private WebClient webClient;
    
    @PostConstruct 
    public void init() {
        this.webClient = WebClient.create("https://api-inference.huggingface.co");
        System.out.println("✅ LLM Provider: HuggingFace (" + model + ")");
    }

    @Override
    public String getRecommendation(String expensesText) {
        String cardContext = cardKnowledgeService.getCardsAsText();
        String prompt = buildRecommendationPrompt(expensesText, cardContext);
        return callHuggingFace(prompt);
    }

    @Override
    public String chat(String userMessage) {
        String cardContext = cardKnowledgeService.getCardsAsText();
        String prompt = buildChatPrompt(userMessage, cardContext);
        return callHuggingFace(prompt);
    }

    @Override
    public boolean isHealthy() {
        try {
            callHuggingFace("test");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getProviderName() {
        return "huggingface:" + model;
    }

    @SuppressWarnings("unchecked")
    private String callHuggingFace(String prompt) {
        Map<String, Object> requestBody = Map.of(
            "inputs", prompt,
            "parameters", Map.of(
                "max_new_tokens", 500,
                "temperature", 0.7,
                "return_full_text", false
            )
        );

        try {
            List<Map<String, Object>> response = webClient.post()
                .uri("/models/" + model)
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

            if (response != null && !response.isEmpty()) {
                return (String) response.get(0).get("generated_text");
            }

            return "No response from HuggingFace.";

        } catch (Exception e) {
            return "Error connecting to HuggingFace: " + e.getMessage();
        }
    }

    private String buildRecommendationPrompt(String expensesText, String cardContext) {
        return String.format(
            "You are a Malaysian credit card advisor. Based on the expenses below, recommend the top 3 credit cards from the list.\n\n" +
            "Monthly Expenses:\n%s\n\n" +
            "Available Credit Cards:\n%s\n\n" +
            "Recommendation:",
            expensesText, cardContext
        );
    }

    private String buildChatPrompt(String userMessage, String cardContext) {
        return String.format(
            "You are a Malaysian credit card advisor. Answer the question using only the cards from the list.\n\n" +
            "Question: %s\n\n" +
            "Available Credit Cards:\n%s\n\n" +
            "Answer:",
            userMessage, cardContext
        );
    }
}
