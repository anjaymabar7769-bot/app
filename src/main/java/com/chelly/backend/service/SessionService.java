package com.chelly.backend.service;

import com.chelly.backend.models.User;
import com.chelly.backend.models.UserSession;
import com.chelly.backend.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final Duration cookieExpiration;

    public SessionService(
            SessionRepository sessionRepository,
            @Value("${session.expiration}") Duration cookieExpiration) {
        this.sessionRepository = sessionRepository;
        this.cookieExpiration = cookieExpiration;
    }

    public UserSession createSession(User user) {
        sessionRepository.findByUser(user).ifPresent(sessionRepository::delete);

        String token = UUID.randomUUID().toString();

        return sessionRepository.save(UserSession.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plus(cookieExpiration))
                .build());
    }

    public Optional<UserSession> findByToken(String token) {
        return sessionRepository.findByToken(token);
    }

    public Optional<UserSession> findByUser(User user) {
        return sessionRepository.findByUser(user);
    }

    public Boolean isSessionValid(UserSession userSession) {
        return userSession.getExpiresAt().isAfter(Instant.now());
    }

    public void deleteSession(UserSession userSession) {
        sessionRepository.delete(userSession);
    }
}
