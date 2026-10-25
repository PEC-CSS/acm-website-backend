package com.pecacm.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.AttendanceMethod;
import com.pecacm.backend.enums.AttendanceSessionStatus;
import com.pecacm.backend.model.ManualAttendanceRequest;
import com.pecacm.backend.model.ScanAttendanceRequest;
import com.pecacm.backend.model.StartAttendanceSessionRequest;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.response.AttendanceRecordResponse;
import com.pecacm.backend.response.AttendanceSessionResponse;
import com.pecacm.backend.response.AttendanceTokenResponse;
import com.pecacm.backend.response.EventUserDetails;
import com.pecacm.backend.services.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceController attendanceController;

    private User testUser;
    private AttendanceSessionResponse sampleSessionResponse;
    private AttendanceRecordResponse sampleRecordResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController).build();

        testUser = User.builder()
                .id(1)
                .name("Admin")
                .email("admin@pec.edu.in")
                .verified(true)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleSessionResponse = AttendanceSessionResponse.builder()
                .id(100)
                .eventId(10)
                .eventTitle("Hackathon")
                .status(AttendanceSessionStatus.ACTIVE)
                .participantXp(1)
                .totalAttendees(5)
                .build();

        sampleRecordResponse = AttendanceRecordResponse.builder()
                .id(1)
                .eventId(10)
                .sessionId(100)
                .userId(1)
                .userName("Admin")
                .userEmail("admin@pec.edu.in")
                .sid(21103001)
                .method(AttendanceMethod.QR_SCAN)
                .xpAwarded(false)
                .build();
    }

    @Test
    void testStartSession_Success() throws Exception {
        StartAttendanceSessionRequest request = StartAttendanceSessionRequest.builder()
                .eventId(10)
                .durationMinutes(20)
                .build();

        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.startSession(any(), eq(testUser))).thenReturn(sampleSessionResponse);

        mockMvc.perform(post("/attendance/sessions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.eventTitle").value("Hackathon"));
    }

    @Test
    void testStopSession_Success() throws Exception {
        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.stopSession(eq(100), eq(testUser))).thenReturn(sampleSessionResponse);

        mockMvc.perform(post("/attendance/sessions/100/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void testGetActiveSession_Success() throws Exception {
        when(attendanceService.getActiveSessionForEvent(10)).thenReturn(sampleSessionResponse);

        mockMvc.perform(get("/attendance/sessions/active/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void testGetRotatingToken_Success() throws Exception {
        AttendanceTokenResponse tokenResponse = AttendanceTokenResponse.builder()
                .token("100.12345.sig")
                .expiresInSeconds(5L)
                .sessionId(100)
                .build();

        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.getRotatingToken(eq(100), eq(testUser))).thenReturn(tokenResponse);

        mockMvc.perform(get("/attendance/sessions/100/token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("100.12345.sig"))
                .andExpect(jsonPath("$.expiresInSeconds").value(5));
    }

    @Test
    void testScanAttendance_Success() throws Exception {
        ScanAttendanceRequest request = ScanAttendanceRequest.builder().token("100.12345.sig").build();

        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.markAttendanceByQr("100.12345.sig", testUser)).thenReturn(sampleRecordResponse);

        mockMvc.perform(post("/attendance/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void testManualAttendance_Success() throws Exception {
        ManualAttendanceRequest request = ManualAttendanceRequest.builder().sid(21103001).build();

        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.markAttendanceManually(eq(100), any(), eq(testUser))).thenReturn(sampleRecordResponse);

        mockMvc.perform(post("/attendance/sessions/100/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sid").value(21103001));
    }

    @Test
    void testGetSessionAttendees_Success() throws Exception {
        when(attendanceService.getSessionAttendees(eq(100), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(sampleRecordResponse)));

        mockMvc.perform(get("/attendance/sessions/100/attendees")
                        .param("offset", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(1));
    }

    @Test
    void testSearchParticipants_Success() throws Exception {
        EventUserDetails userDetails = new EventUserDetails(1, "admin@pec.edu.in", "Admin", null);
        when(attendanceService.searchParticipants("admin")).thenReturn(List.of(userDetails));

        mockMvc.perform(get("/attendance/participants/search")
                        .param("query", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@pec.edu.in"));
    }

    @Test
    void testGetAttendeeEmails_Success() throws Exception {
        when(attendanceService.getAttendeeEmailsForEvent(10)).thenReturn(List.of("admin@pec.edu.in"));

        mockMvc.perform(get("/attendance/events/10/attendee-emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("admin@pec.edu.in"));
    }

    @Test
    void testAwardXp_Success() throws Exception {
        when(attendanceService.awardAttendanceXp(10, 5)).thenReturn(12);

        mockMvc.perform(post("/attendance/events/10/award-xp")
                        .param("xpAmount", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.awardedCount").value(12));
    }

    @Test
    void testGetSessionById_Success() throws Exception {
        when(attendanceService.getSessionById(100)).thenReturn(sampleSessionResponse);

        mockMvc.perform(get("/attendance/sessions/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void testGetSessionsForEvent_Success() throws Exception {
        when(attendanceService.getSessionsForEvent(10)).thenReturn(List.of(sampleSessionResponse));

        mockMvc.perform(get("/attendance/events/10/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void testGetMyAttendanceStatus_Found() throws Exception {
        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.getUserAttendanceForEvent(10, testUser)).thenReturn(Optional.of(sampleRecordResponse));

        mockMvc.perform(get("/attendance/events/10/my-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void testGetMyAttendanceStatus_NotFound() throws Exception {
        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.getUserAttendanceForEvent(10, testUser)).thenReturn(Optional.empty());

        mockMvc.perform(get("/attendance/events/10/my-status"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testStreamRotatingTokens_Success() throws Exception {
        when(userRepository.findByEmail("admin@pec.edu.in")).thenReturn(Optional.of(testUser));
        when(attendanceService.streamRotatingTokens(eq(100), eq(testUser)))
                .thenReturn(new org.springframework.web.servlet.mvc.method.annotation.SseEmitter());

        mockMvc.perform(get("/attendance/sessions/100/token/stream"))
                .andExpect(status().isOk());
    }
}
