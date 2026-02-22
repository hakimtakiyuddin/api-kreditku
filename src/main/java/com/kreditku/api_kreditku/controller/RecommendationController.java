package com.kreditku.api_kreditku.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kreditku.api_kreditku.exception.RateLimitExceededException;
import com.kreditku.api_kreditku.model.CreditCard;
import com.kreditku.api_kreditku.model.dto.ChatResponse;
import com.kreditku.api_kreditku.service.chat.ChatService;
import com.kreditku.api_kreditku.service.knowledge.CardKnowledgeService;
import com.kreditku.api_kreditku.service.llm.LlmService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "KreditKu API", description = "Credit card recommendation endpoints")
public class RecommendationController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private LlmService llmService;

    @Autowired
    private CardKnowledgeService cardKnowledgeService;

    @Operation(summary = "Upload expense file and get card recommendation")
    @PostMapping(value = "/recommend", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ChatResponse> recommend(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        // Generate anonymous user ID if not provided
        if (userId == null || userId.isBlank()) {
            userId = "anonymous-" + System.currentTimeMillis();
        }

        try {
            ChatResponse response = chatService.sendMessage(userId, null, file);
            return ResponseEntity.ok(response);
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ChatResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Failed to process file: " + e.getMessage()));
        }
    }

    @Operation(summary = "Ask a question about credit cards")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        String message = body.get("message");

        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Message cannot be empty."));
        }

        // Generate anonymous user ID if not provided
        if (userId == null || userId.isBlank()) {
            userId = "anonymous-" + System.currentTimeMillis();
        }

        try {
            ChatResponse response = chatService.sendMessage(userId, message, null);
            return ResponseEntity.ok(response);
        } catch (RateLimitExceededException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ChatResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ChatResponse("Error: " + e.getMessage()));
        }
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