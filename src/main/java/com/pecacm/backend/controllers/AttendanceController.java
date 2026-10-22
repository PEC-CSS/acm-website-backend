package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.ManualAttendanceRequest;
import com.pecacm.backend.model.ScanAttendanceRequest;
import com.pecacm.backend.model.StartAttendanceSessionRequest;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.response.AttendanceRecordResponse;
import com.pecacm.backend.response.AttendanceSessionResponse;
import com.pecacm.backend.response.AttendanceTokenResponse;
import com.pecacm.backend.response.EventUserDetails;
import com.pecacm.backend.services.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    @Autowired
    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @PostMapping("/sessions/start")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<AttendanceSessionResponse> startSession(@RequestBody StartAttendanceSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.startSession(request, getAuthenticatedUser()));
    }

    @PostMapping("/sessions/{sessionId}/stop")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<AttendanceSessionResponse> stopSession(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceService.stopSession(sessionId, getAuthenticatedUser()));
    }

    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<AttendanceSessionResponse> getSessionById(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceService.getSessionById(sessionId));
    }

    @GetMapping("/events/{eventId}/sessions")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<List<AttendanceSessionResponse>> getSessionsForEvent(@PathVariable Integer eventId) {
        return ResponseEntity.ok(attendanceService.getSessionsForEvent(eventId));
    }

    @GetMapping("/sessions/active/{eventId}")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<AttendanceSessionResponse> getActiveSession(@PathVariable Integer eventId) {
        return ResponseEntity.ok(attendanceService.getActiveSessionForEvent(eventId));
    }

    @GetMapping("/sessions/{sessionId}/token")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<AttendanceTokenResponse> getRotatingToken(@PathVariable Integer sessionId) {
        return ResponseEntity.ok(attendanceService.getRotatingToken(sessionId, getAuthenticatedUser()));
    }

    @PostMapping("/scan")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<AttendanceRecordResponse> scanAttendance(@RequestBody ScanAttendanceRequest request) {
        if (request == null || request.getToken() == null || request.getToken().isBlank()) {
            throw new AcmException("QR token is required", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(attendanceService.markAttendanceByQr(request.getToken(), getAuthenticatedUser()));
    }

    @GetMapping("/events/{eventId}/my-status")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<AttendanceRecordResponse> getMyAttendanceStatus(@PathVariable Integer eventId) {
        return attendanceService.getUserAttendanceForEvent(eventId, getAuthenticatedUser())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/sessions/{sessionId}/manual")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<AttendanceRecordResponse> manualAttendance(
            @PathVariable Integer sessionId,
            @RequestBody ManualAttendanceRequest request
    ) {
        return ResponseEntity.ok(attendanceService.markAttendanceManually(sessionId, request, getAuthenticatedUser()));
    }

    @GetMapping("/sessions/{sessionId}/attendees")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<Page<AttendanceRecordResponse>> getSessionAttendees(
            @PathVariable Integer sessionId,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) {
        if (offset < 0 || pageSize <= 0) {
            throw new AcmException("Invalid pagination parameters", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(attendanceService.getSessionAttendees(sessionId, PageRequest.of(offset, pageSize)));
    }

    @GetMapping("/participants/search")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<List<EventUserDetails>> searchParticipants(@RequestParam String query) {
        return ResponseEntity.ok(attendanceService.searchParticipants(query));
    }

    @GetMapping("/events/{eventId}/attendee-emails")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<List<String>> getAttendeeEmails(@PathVariable Integer eventId) {
        return ResponseEntity.ok(attendanceService.getAttendeeEmailsForEvent(eventId));
    }

    @PostMapping("/events/{eventId}/award-xp")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public ResponseEntity<Map<String, Object>> awardXp(
            @PathVariable Integer eventId,
            @RequestParam(required = false) Integer xpAmount
    ) {
        int awarded = attendanceService.awardAttendanceXp(eventId, xpAmount);
        return ResponseEntity.ok(Map.of(
                "awardedCount", awarded,
                "message", "Attendance XP successfully awarded"
        ));
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AcmException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getPrincipal().toString();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AcmException("Authenticated user not found", HttpStatus.UNAUTHORIZED));
    }
}
