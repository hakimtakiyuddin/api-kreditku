package com.kreditku.api_kreditku.service.knowledge;

import java.util.List;

import com.kreditku.api_kreditku.model.CreditCard;

public interface CardKnowledgeService {
    List<CreditCard> getAllCards();

    List<CreditCard> findRelevantCards(String query);

    String getCardsAsText();
}
