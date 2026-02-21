package com.kreditku.api_kreditku.service.llm;

public interface LlmService {
    String getRecommendation(String expensesText);

    String chat(String userMessage);

    boolean isHealthy();

    String getProviderName();
}
