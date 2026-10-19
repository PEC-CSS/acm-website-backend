package com.pecacm.backend.services;

import com.pecacm.backend.entities.AttendanceSession;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.response.AttendanceTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class AttendanceTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public AttendanceTokenResponse generateToken(AttendanceSession session) {
        if (session == null || session.getEvent() == null) {
            throw new AcmException("Invalid session data for token generation", HttpStatus.BAD_REQUEST);
        }

        long windowSeconds = session.getTokenExpirySeconds() != null ? session.getTokenExpirySeconds() : 5L;
        long nowMillis = System.currentTimeMillis();
        long windowIndex = nowMillis / (windowSeconds * 1000L);
        long elapsedInWindow = (nowMillis / 1000L) % windowSeconds;
        long expiresInSeconds = windowSeconds - elapsedInWindow;

        String message = session.getId() + ":" + session.getEvent().getId() + ":" + windowIndex;
        String signature = sign(message, session.getSecretKey());
        String token = session.getId() + "." + windowIndex + "." + signature;

        return AttendanceTokenResponse.builder()
                .token(token)
                .sessionId(session.getId())
                .eventId(session.getEvent().getId())
                .expiresInSeconds(expiresInSeconds)
                .windowSeconds((int) windowSeconds)
                .build();
    }

    public boolean validateToken(AttendanceSession session, String token) {
        if (session == null || token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.trim().split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            int tokenSessionId = Integer.parseInt(parts[0]);
            long tokenWindow = Long.parseLong(parts[1]);
            String tokenSignature = parts[2];

            if (!session.getId().equals(tokenSessionId)) {
                return false;
            }

            long windowSeconds = session.getTokenExpirySeconds() != null ? session.getTokenExpirySeconds() : 5L;
            long graceSeconds = session.getGracePeriodSeconds() != null ? session.getGracePeriodSeconds() : 5L;
            long allowedGraceWindows = (graceSeconds + windowSeconds - 1) / windowSeconds;
            long currentWindow = System.currentTimeMillis() / (windowSeconds * 1000L);

            if (tokenWindow > currentWindow || tokenWindow < (currentWindow - allowedGraceWindows)) {
                return false;
            }

            String message = session.getId() + ":" + session.getEvent().getId() + ":" + tokenWindow;
            String expectedSignature = sign(message, session.getSecretKey());

            return MessageDigest.isEqual(
                    tokenSignature.getBytes(StandardCharsets.UTF_8),
                    expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public Integer extractSessionId(String token) {
        if (token == null || token.isBlank()) {
            throw new AcmException("Token cannot be empty", HttpStatus.BAD_REQUEST);
        }

        String[] parts = token.trim().split("\\.");
        if (parts.length != 3) {
            throw new AcmException("Invalid token format", HttpStatus.BAD_REQUEST);
        }

        try {
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException ex) {
            throw new AcmException("Invalid session ID in token", HttpStatus.BAD_REQUEST);
        }
    }

    private String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception ex) {
            throw new AcmException("Failed to generate token signature", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
