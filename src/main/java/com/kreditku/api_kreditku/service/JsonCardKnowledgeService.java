package com.kreditku.api_kreditku.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kreditku.api_kreditku.model.CreditCard;

import jakarta.annotation.PostConstruct;

@Service
public class JsonCardKnowledgeService implements CardKnowledgeService {
    private List<CreditCard> cards;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadCards() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/credit-card.json");
        cards = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<CreditCard>>() {
                });
        System.out.println("✅ Loaded " + cards.size() + " credit cards from knowledge base.");
    }

    @Override
    public List<CreditCard> getAllCards() {
        return cards;
    }

    @Override
    public List<CreditCard> findRelevantCards(String query) {
        // Simple keyword match for now
        // TODO: replace with vector similarity search when card list grows
        String lowerQuery = query.toLowerCase();
        List<CreditCard> relevant = cards.stream()
                .filter(card -> card.getBestFor().toLowerCase().contains(lowerQuery) ||
                        card.getBenefits().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        return relevant.isEmpty() ? cards : relevant;
    }

    @Override
    public String getCardsAsText() {
        StringBuilder sb = new StringBuilder();
        for (CreditCard card : cards) {
            sb.append("Card: ").append(card.getName()).append("\n");
            sb.append("Bank: ").append(card.getBank()).append("\n");
            sb.append("Benefits: ").append(card.getBenefits()).append("\n");
            sb.append("Best For: ").append(card.getBestFor()).append("\n");
            sb.append("Annual Fee: ").append(card.getAnnualFee()).append("\n");
            sb.append("Min Income: ").append(card.getMinIncome()).append("\n");
            sb.append("---\n");
        }
        return sb.toString();
    }
}
