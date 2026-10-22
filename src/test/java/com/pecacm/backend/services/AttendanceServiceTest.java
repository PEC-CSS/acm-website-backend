package com.pecacm.backend.services;

import com.pecacm.backend.entities.AttendanceRecord;
import com.pecacm.backend.entities.AttendanceSession;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.AttendanceMethod;
import com.pecacm.backend.enums.AttendanceSessionStatus;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.ManualAttendanceRequest;
import com.pecacm.backend.model.StartAttendanceSessionRequest;
import com.pecacm.backend.repository.AttendanceRecordRepository;
import com.pecacm.backend.repository.AttendanceSessionRepository;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.repository.TransactionRepository;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.response.AttendanceRecordResponse;
import com.pecacm.backend.response.AttendanceSessionResponse;
import com.pecacm.backend.response.EventUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository attendanceSessionRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AttendanceTokenService attendanceTokenService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Event testEvent;
    private User adminUser;
    private User studentUser;
    private AttendanceSession testSession;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
                .id(10)
                .title("Annual Hackathon")
                .ended(false)
                .build();

        adminUser = User.builder()
                .id(1)
                .name("Admin")
                .email("admin@pec.edu.in")
                .verified(true)
                .build();

        studentUser = User.builder()
                .id(2)
                .name("Student")
                .email("student@pec.edu.in")
                .sid(21103001)
                .verified(true)
                .xp(10)
                .build();

        testSession = AttendanceSession.builder()
                .id(100)
                .event(testEvent)
                .secretKey("secret-123")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusMinutes(15))
                .status(AttendanceSessionStatus.ACTIVE)
                .createdById(1)
                .tokenExpirySeconds(5)
                .gracePeriodSeconds(5)
                .build();
    }

    @Test
    void testStartSession_Success() {
        StartAttendanceSessionRequest request = StartAttendanceSessionRequest.builder()
                .eventId(10)
                .durationMinutes(20)
                .build();

        when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
        when(attendanceSessionRepository.findFirstByEventIdAndStatusOrderByStartTimeDesc(10, AttendanceSessionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(attendanceSessionRepository.save(any(AttendanceSession.class))).thenReturn(testSession);
        when(attendanceRecordRepository.countBySessionId(testSession.getId())).thenReturn(0L);

        AttendanceSessionResponse response = attendanceService.startSession(request, adminUser);

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals("Annual Hackathon", response.getEventTitle());
        assertEquals(AttendanceSessionStatus.ACTIVE, response.getStatus());
    }

    @Test
    void testStartSession_EndedEventThrows() {
        testEvent.setEnded(true);
        StartAttendanceSessionRequest request = StartAttendanceSessionRequest.builder()
                .eventId(10)
                .build();

        when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));

        AcmException exception = assertThrows(AcmException.class, () ->
                attendanceService.startSession(request, adminUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Cannot start attendance session for an ended event", exception.getMessage());
    }

    @Test
    void testMarkAttendanceByQr_Success() {
        String token = "100.12345.sig";
        when(attendanceTokenService.extractSessionId(token)).thenReturn(100);
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(attendanceRecordRepository.existsByEventIdAndUserId(10, 2)).thenReturn(false);
        when(attendanceTokenService.validateToken(testSession, token)).thenReturn(true);

        AttendanceRecord savedRecord = AttendanceRecord.builder()
                .id(1)
                .event(testEvent)
                .session(testSession)
                .user(studentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.QR_SCAN)
                .xpAwarded(false)
                .build();

        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(savedRecord);

        AttendanceRecordResponse response = attendanceService.markAttendanceByQr(token, studentUser);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertEquals(2, response.getUserId());
        assertEquals(AttendanceMethod.QR_SCAN, response.getMethod());
        assertFalse(response.getXpAwarded());
    }

    @Test
    void testMarkAttendanceByQr_DuplicateThrows() {
        String token = "100.12345.sig";
        when(attendanceTokenService.extractSessionId(token)).thenReturn(100);
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(attendanceRecordRepository.existsByEventIdAndUserId(10, 2)).thenReturn(true);

        AcmException exception = assertThrows(AcmException.class, () ->
                attendanceService.markAttendanceByQr(token, studentUser));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Attendance already marked for this event", exception.getMessage());
    }

    @Test
    void testMarkAttendanceByQr_InvalidTokenThrows() {
        String token = "100.12345.bad";
        when(attendanceTokenService.extractSessionId(token)).thenReturn(100);
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(attendanceRecordRepository.existsByEventIdAndUserId(10, 2)).thenReturn(false);
        when(attendanceTokenService.validateToken(testSession, token)).thenReturn(false);

        AcmException exception = assertThrows(AcmException.class, () ->
                attendanceService.markAttendanceByQr(token, studentUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Invalid or expired attendance QR token", exception.getMessage());
    }

    @Test
    void testMarkAttendanceManually_BySid_Success() {
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(userRepository.findBySid(21103001)).thenReturn(Optional.of(studentUser));
        when(attendanceRecordRepository.existsByEventIdAndUserId(10, 2)).thenReturn(false);

        AttendanceRecord savedRecord = AttendanceRecord.builder()
                .id(2)
                .event(testEvent)
                .session(testSession)
                .user(studentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.MANUAL_ADMIN)
                .markedByAdminId(1)
                .xpAwarded(false)
                .build();

        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(savedRecord);

        ManualAttendanceRequest request = ManualAttendanceRequest.builder().sid(21103001).build();
        AttendanceRecordResponse response = attendanceService.markAttendanceManually(100, request, adminUser);

        assertNotNull(response);
        assertEquals(2, response.getId());
        assertEquals(AttendanceMethod.MANUAL_ADMIN, response.getMethod());
        assertEquals(1, response.getMarkedByAdminId());
    }

    @Test
    void testMarkAttendanceManually_ByEmail_Success() {
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(userRepository.findByEmail("student@pec.edu.in")).thenReturn(Optional.of(studentUser));
        when(attendanceRecordRepository.existsByEventIdAndUserId(10, 2)).thenReturn(false);

        AttendanceRecord savedRecord = AttendanceRecord.builder()
                .id(3)
                .event(testEvent)
                .session(testSession)
                .user(studentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.MANUAL_ADMIN)
                .markedByAdminId(1)
                .xpAwarded(false)
                .build();

        when(attendanceRecordRepository.save(any(AttendanceRecord.class))).thenReturn(savedRecord);

        ManualAttendanceRequest request = ManualAttendanceRequest.builder().email("student@pec.edu.in").build();
        AttendanceRecordResponse response = attendanceService.markAttendanceManually(100, request, adminUser);

        assertNotNull(response);
        assertEquals(3, response.getId());
        assertEquals(AttendanceMethod.MANUAL_ADMIN, response.getMethod());
        assertEquals(1, response.getMarkedByAdminId());
    }

    @Test
    void testSearchParticipants() {
        when(userRepository.findAllBySearchQuery("student", true))
                .thenReturn(List.of(studentUser));

        List<EventUserDetails> results = attendanceService.searchParticipants("student");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("student@pec.edu.in", results.get(0).getEmail());
    }

    @Test
    void testAwardAttendanceXp_Success() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1)
                .event(testEvent)
                .session(testSession)
                .user(studentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.QR_SCAN)
                .xpAwarded(false)
                .build();

        when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
        when(attendanceRecordRepository.findByEventIdAndXpAwardedFalse(10))
                .thenReturn(List.of(record));

        int awarded = attendanceService.awardAttendanceXp(10, 5);

        assertEquals(1, awarded);
        assertEquals(15, studentUser.getXp());
        assertTrue(record.getXpAwarded());
        verify(transactionRepository, times(1)).saveAll(any());
        verify(userRepository, times(1)).saveAll(any());
        verify(attendanceRecordRepository, times(1)).saveAll(any());
    }

    @Test
    void testMarkAttendanceByQr_UnverifiedUserThrows() {
        studentUser.setVerified(false);
        AcmException exception = assertThrows(AcmException.class, () ->
                attendanceService.markAttendanceByQr("token", studentUser));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("User account must be verified to mark attendance", exception.getMessage());
    }

    @Test
    void testMarkAttendanceManually_InvalidSidThrows() {
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));

        ManualAttendanceRequest request = ManualAttendanceRequest.builder().sid(12345).build();
        AcmException exception = assertThrows(AcmException.class, () ->
                attendanceService.markAttendanceManually(100, request, adminUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Student ID (SID) must be an 8-digit number", exception.getMessage());
    }

    @Test
    void testMarkAttendanceManually_UnverifiedStudentThrows() {
        studentUser.setVerified(false);
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(userRepository.findBySid(21103001)).thenReturn(Optional.of(studentUser));

        ManualAttendanceRequest request = ManualAttendanceRequest.builder().sid(21103001).build();
        AcmException exception = assertThrows(AcmException.class, () ->
                attendanceService.markAttendanceManually(100, request, adminUser));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Student account is not verified", exception.getMessage());
    }

    @Test
    void testAwardAttendanceXp_DefaultSessionXp() {
        testSession.setParticipantXp(3);
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1)
                .event(testEvent)
                .session(testSession)
                .user(studentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.QR_SCAN)
                .xpAwarded(false)
                .build();

        when(eventRepository.findById(10)).thenReturn(Optional.of(testEvent));
        when(attendanceRecordRepository.findByEventIdAndXpAwardedFalse(10))
                .thenReturn(List.of(record));

        int awarded = attendanceService.awardAttendanceXp(10, null);

        assertEquals(1, awarded);
        assertEquals(13, studentUser.getXp());
        assertTrue(record.getXpAwarded());
    }

    @Test
    void testGetSessionById_Success() {
        when(attendanceSessionRepository.findById(100)).thenReturn(Optional.of(testSession));
        when(attendanceRecordRepository.countBySessionId(100)).thenReturn(2L);

        AttendanceSessionResponse response = attendanceService.getSessionById(100);

        assertNotNull(response);
        assertEquals(100, response.getId());
        assertEquals(2L, response.getTotalAttendees());
    }

    @Test
    void testGetSessionsForEvent_Success() {
        when(eventRepository.existsById(10)).thenReturn(true);
        when(attendanceSessionRepository.findByEventIdOrderByStartTimeDesc(10)).thenReturn(List.of(testSession));
        when(attendanceRecordRepository.countBySessionId(100)).thenReturn(0L);

        List<AttendanceSessionResponse> sessions = attendanceService.getSessionsForEvent(10);

        assertNotNull(sessions);
        assertEquals(1, sessions.size());
        assertEquals(100, sessions.get(0).getId());
    }

    @Test
    void testGetUserAttendanceForEvent_Found() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1)
                .event(testEvent)
                .session(testSession)
                .user(studentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.QR_SCAN)
                .xpAwarded(false)
                .build();

        when(attendanceRecordRepository.findByEventIdAndUserId(10, 2)).thenReturn(Optional.of(record));

        Optional<AttendanceRecordResponse> response = attendanceService.getUserAttendanceForEvent(10, studentUser);

        assertTrue(response.isPresent());
        assertEquals(1, response.get().getId());
        assertEquals(2, response.get().getUserId());
    }

    @Test
    void testGetUserAttendanceForEvent_NotFound() {
        when(attendanceRecordRepository.findByEventIdAndUserId(10, 2)).thenReturn(Optional.empty());

        Optional<AttendanceRecordResponse> response = attendanceService.getUserAttendanceForEvent(10, studentUser);

        assertTrue(response.isEmpty());
    }
}


