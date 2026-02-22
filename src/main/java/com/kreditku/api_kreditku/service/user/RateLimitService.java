package com.kreditku.api_kreditku.service.user;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kreditku.api_kreditku.model.entity.User;
import com.kreditku.api_kreditku.repository.UserRepository;

@Service
public class RateLimitService {

    private static final int FREE_DAILY_LIMIT = 20;

    @Autowired
    private UserRepository userRepository;

    public boolean canSendMessage(String userId) {
        User user = userRepository.findById(userId)
                .orElseGet(() -> createNewUser(userId));

        // Reset counter if it's a new day
        if (!user.getLastResetDate().equals(LocalDate.now())) {
            user.resetDailyCount();
        }

        // Check limit
        if (user.getDailyMessageCount() >= FREE_DAILY_LIMIT) {
            return false;
        }

        // Increment and save
        user.incrementMessageCount();
        userRepository.save(user);

        return true;
    }

    public int getRemainingMessages(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return FREE_DAILY_LIMIT;
        }

        // Reset if new day
        if (!user.getLastResetDate().equals(LocalDate.now())) {
            return FREE_DAILY_LIMIT;
        }

        return Math.max(0, FREE_DAILY_LIMIT - user.getDailyMessageCount());
    }

    private User createNewUser(String userId) {
        User newUser = new User(userId);
        return userRepository.save(newUser);
    }
}
