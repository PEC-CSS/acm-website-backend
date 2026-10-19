package com.pecacm.backend.services;

import com.pecacm.backend.entities.AttendanceSession;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.response.AttendanceTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceTokenServiceTest {

    private AttendanceTokenService tokenService;
    private AttendanceSession session;

    @BeforeEach
    void setUp() {
        tokenService = new AttendanceTokenService();

        Event event = Event.builder()
                .id(101)
                .title("Tech Workshop")
                .build();

        session = AttendanceSession.builder()
                .id(1)
                .event(event)
                .secretKey(UUID.randomUUID().toString())
                .tokenExpirySeconds(5)
                .gracePeriodSeconds(5)
                .build();
    }

    @Test
    void testGenerateToken() {
        AttendanceTokenResponse response = tokenService.generateToken(session);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(1, response.getSessionId());
        assertEquals(101, response.getEventId());
        assertEquals(5, response.getWindowSeconds());
        assertTrue(response.getExpiresInSeconds() > 0 && response.getExpiresInSeconds() <= 5);

        String[] parts = response.getToken().split("\\.");
        assertEquals(3, parts.length);
        assertEquals("1", parts[0]);
    }

    @Test
    void testValidateTokenSuccess() {
        AttendanceTokenResponse response = tokenService.generateToken(session);
        boolean isValid = tokenService.validateToken(session, response.getToken());

        assertTrue(isValid);
    }

    @Test
    void testValidateTokenTamperedSignature() {
        AttendanceTokenResponse response = tokenService.generateToken(session);
        String tamperedToken = response.getToken() + "bad";

        assertFalse(tokenService.validateToken(session, tamperedToken));
    }

    @Test
    void testValidateTokenWrongSession() {
        AttendanceTokenResponse response = tokenService.generateToken(session);

        AttendanceSession differentSession = AttendanceSession.builder()
                .id(2)
                .event(session.getEvent())
                .secretKey(UUID.randomUUID().toString())
                .tokenExpirySeconds(5)
                .gracePeriodSeconds(5)
                .build();

        assertFalse(tokenService.validateToken(differentSession, response.getToken()));
    }

    @Test
    void testExtractSessionId() {
        AttendanceTokenResponse response = tokenService.generateToken(session);
        Integer extractedId = tokenService.extractSessionId(response.getToken());

        assertEquals(1, extractedId);
    }
}
