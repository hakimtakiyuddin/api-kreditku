package com.kreditku.api_kreditku.service.chat;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kreditku.api_kreditku.exception.RateLimitExceededException;
import com.kreditku.api_kreditku.model.dto.ChatResponse;
import com.kreditku.api_kreditku.model.entity.Conversation;
import com.kreditku.api_kreditku.model.entity.Message;
import com.kreditku.api_kreditku.repository.ConversationRepository;
import com.kreditku.api_kreditku.repository.MessageRepository;
import com.kreditku.api_kreditku.service.llm.LlmService;
import com.kreditku.api_kreditku.service.parser.ExcelService;
import com.kreditku.api_kreditku.service.user.RateLimitService;

@Service
public class ChatService {
    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private LlmService llmService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    public ChatResponse sendMessage(String userId, String text, MultipartFile file) throws Exception {
        // 1. Check rate limit
        if (!rateLimitService.canSendMessage(userId)) {
            throw new RateLimitExceededException(
                    "Daily message limit reached. You have " +
                            rateLimitService.getRemainingMessages(userId) +
                            " messages remaining today.");
        }

        // 2. Get or create conversation
        Conversation conversation = createConversation(userId);

        // 3. Save user message
        String userMessageText = text != null ? text : "Here is my expense file.";
        saveMessage(conversation, "user", userMessageText, file != null ? file.getOriginalFilename() : null);

        // 4. Process with LLM
        String aiResponse;
        if (file != null) {
            Map<String, Double> expenses = excelService.parseExpenses(file);
            String expensesText = excelService.formatExpensesAsText(expenses);
            aiResponse = llmService.getRecommendation(expensesText);
        } else {
            aiResponse = llmService.chat(text);
        }

        // 5. Save AI response
        saveMessage(conversation, "ai", aiResponse, null);

        // 6. Return response
        return new ChatResponse(
                aiResponse,
                conversation.getId(),
                llmService.getProviderName(),
                rateLimitService.getRemainingMessages(userId));
    }

    private Conversation createConversation(String userId) {
        String conversationId = UUID.randomUUID().toString();
        Conversation conversation = new Conversation(
                conversationId,
                userId,
                "New conversation");
        return conversationRepository.save(conversation);
    }

    private void saveMessage(Conversation conversation, String role, String content, String fileName) {
        String messageId = UUID.randomUUID().toString();
        Message message = new Message(messageId, conversation, role, content);
        message.setFileName(fileName);
        messageRepository.save(message);

        // Update conversation timestamp
        conversation.touch();
        conversationRepository.save(conversation);
    }
}
