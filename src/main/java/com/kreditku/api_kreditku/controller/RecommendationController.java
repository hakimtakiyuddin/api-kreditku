package com.kreditku.api_kreditku.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kreditku.api_kreditku.model.CreditCard;
import com.kreditku.api_kreditku.model.RecommendationResponse;
import com.kreditku.api_kreditku.service.CardKnowledgeService;
import com.kreditku.api_kreditku.service.ExcelService;
import com.kreditku.api_kreditku.service.LlmService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "KreditKu API", description = "Credit card recommendation endpoints")
public class RecommendationController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private LlmService llmService; // ← interface, not a specific impl

    @Autowired
    private CardKnowledgeService cardKnowledgeService;

    @Operation(summary = "Upload expense file and get card recommendation")
    @PostMapping(value = "/recommend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecommendationResponse> recommend(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Double> expenses = excelService.parseExpenses(file);

            if (expenses.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse("No expense data found in file.", false));
            }

            String expensesText = excelService.formatExpensesAsText(expenses);
            String recommendation = llmService.getRecommendation(expensesText);

            if (recommendation.startsWith("Error")) {
                return ResponseEntity.badRequest()
                        .body(new RecommendationResponse(recommendation, false));
            }

            return ResponseEntity.ok(
                    new RecommendationResponse(recommendation, llmService.getProviderName()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new RecommendationResponse("Failed to process file: " + e.getMessage(), false));
        }
    }

    @Operation(summary = "Ask a question about credit cards")
    @PostMapping("/chat")
    public ResponseEntity<RecommendationResponse> chat(
            @RequestBody Map<String, String> body) {
        String message = body.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new RecommendationResponse("Message cannot be empty.", false));
        }

        String response = llmService.chat(message);
        return ResponseEntity.ok(
                new RecommendationResponse(response, llmService.getProviderName()));
    }

    @Operation(summary = "Get all available credit cards")
    @GetMapping("/cards")
    public ResponseEntity<List<CreditCard>> getCards() {
        return ResponseEntity.ok(cardKnowledgeService.getAllCards());
    }

    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "backend", "up",
                "llmProvider", llmService.getProviderName(),
                "llmHealthy", llmService.isHealthy()));
    }
}
