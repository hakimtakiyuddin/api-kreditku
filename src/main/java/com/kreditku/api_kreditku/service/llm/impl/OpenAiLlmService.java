package com.kreditku.api_kreditku.service.llm.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.kreditku.api_kreditku.service.llm.LlmService;

@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "openai")
public class OpenAiLlmService implements LlmService {
    @Override
    public String getRecommendation(String expensesText) {
        return "OpenAI provider not yet implemented.";
    }

    @Override
    public String chat(String userMessage) {
        return "OpenAI provider not yet implemented.";
    }

    @Override
    public boolean isHealthy() {
        return false;
    }

    @Override
    public String getProviderName() {
        return "openai";
    }
}
