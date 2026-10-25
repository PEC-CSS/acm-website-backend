package com.pecacm.backend.services;

import com.pecacm.backend.entities.AttendanceRecord;
import com.pecacm.backend.entities.AttendanceSession;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.AttendanceMethod;
import com.pecacm.backend.enums.AttendanceSessionStatus;
import com.pecacm.backend.enums.EventRole;
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
import com.pecacm.backend.response.AttendanceTokenResponse;
import com.pecacm.backend.response.EventUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AttendanceTokenService attendanceTokenService;

    @Autowired
    public AttendanceService(
            AttendanceSessionRepository attendanceSessionRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            AttendanceTokenService attendanceTokenService
    ) {
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.attendanceTokenService = attendanceTokenService;
    }

    @Transactional
    public AttendanceSessionResponse startSession(StartAttendanceSessionRequest request, User adminUser) {
        if (request == null || request.getEventId() == null) {
            throw new AcmException("Event ID is required", HttpStatus.BAD_REQUEST);
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new AcmException("Event not found", HttpStatus.NOT_FOUND));

        if (event.isEnded()) {
            throw new AcmException("Cannot start attendance session for an ended event", HttpStatus.BAD_REQUEST);
        }

        Optional<AttendanceSession> activeOpt = attendanceSessionRepository
                .findFirstByEventIdAndStatusOrderByStartTimeDesc(event.getId(), AttendanceSessionStatus.ACTIVE);

        if (activeOpt.isPresent()) {
            AttendanceSession existing = activeOpt.get();
            if (LocalDateTime.now().isBefore(existing.getEndTime())) {
                existing.setStatus(AttendanceSessionStatus.STOPPED);
                existing.setEndTime(LocalDateTime.now());
                attendanceSessionRepository.save(existing);
            } else {
                existing.setStatus(AttendanceSessionStatus.EXPIRED);
                attendanceSessionRepository.save(existing);
            }
        }

        int duration = request.getDurationMinutes() != null && request.getDurationMinutes() > 0
                ? request.getDurationMinutes() : 15;
        int tokenExpiry = request.getTokenExpirySeconds() != null && request.getTokenExpirySeconds() > 0
                ? request.getTokenExpirySeconds() : 5;
        int gracePeriod = request.getGracePeriodSeconds() != null && request.getGracePeriodSeconds() > 0
                ? request.getGracePeriodSeconds() : 5;
        int participantXp = request.getParticipantXp() != null && request.getParticipantXp() > 0
                ? request.getParticipantXp() : 1;

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = AttendanceSession.builder()
                .event(event)
                .secretKey(UUID.randomUUID().toString())
                .startTime(now)
                .endTime(now.plusMinutes(duration))
                .status(AttendanceSessionStatus.ACTIVE)
                .createdById(adminUser.getId())
                .tokenExpirySeconds(tokenExpiry)
                .gracePeriodSeconds(gracePeriod)
                .participantXp(participantXp)
                .build();

        AttendanceSession saved = attendanceSessionRepository.save(session);
        return mapToSessionResponse(saved);
    }

    @Transactional
    public AttendanceSessionResponse stopSession(Integer sessionId, User adminUser) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AcmException("Attendance session not found", HttpStatus.NOT_FOUND));

        if (session.getStatus() == AttendanceSessionStatus.ACTIVE) {
            session.setStatus(AttendanceSessionStatus.STOPPED);
            session.setEndTime(LocalDateTime.now());
            session = attendanceSessionRepository.save(session);
        }

        return mapToSessionResponse(session);
    }

    @Transactional
    public int expireStaleSessions() {
        List<AttendanceSession> stale = attendanceSessionRepository
                .findAllByStatusAndEndTimeBefore(AttendanceSessionStatus.ACTIVE, LocalDateTime.now());

        if (stale.isEmpty()) {
            return 0;
        }

        stale.forEach(session -> session.setStatus(AttendanceSessionStatus.EXPIRED));
        attendanceSessionRepository.saveAll(stale);
        return stale.size();
    }

    public AttendanceSessionResponse getActiveSessionForEvent(Integer eventId) {
        AttendanceSession session = attendanceSessionRepository
                .findFirstByEventIdAndStatusOrderByStartTimeDesc(eventId, AttendanceSessionStatus.ACTIVE)
                .orElseThrow(() -> new AcmException("No active attendance session found for event", HttpStatus.NOT_FOUND));

        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            session.setStatus(AttendanceSessionStatus.EXPIRED);
            attendanceSessionRepository.save(session);
            throw new AcmException("Attendance session has expired", HttpStatus.NOT_FOUND);
        }

        return mapToSessionResponse(session);
    }

    public AttendanceSessionResponse getSessionById(Integer sessionId) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AcmException("Attendance session not found", HttpStatus.NOT_FOUND));
        return mapToSessionResponse(session);
    }

    public List<AttendanceSessionResponse> getSessionsForEvent(Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new AcmException("Event not found", HttpStatus.NOT_FOUND);
        }
        return attendanceSessionRepository.findByEventIdOrderByStartTimeDesc(eventId).stream()
                .map(this::mapToSessionResponse)
                .toList();
    }

    public Optional<AttendanceRecordResponse> getUserAttendanceForEvent(Integer eventId, User user) {
        if (user == null || eventId == null) {
            return Optional.empty();
        }
        return attendanceRecordRepository.findByEventIdAndUserId(eventId, user.getId())
                .map(this::mapToRecordResponse);
    }

    @Transactional
    public AttendanceTokenResponse getRotatingToken(Integer sessionId, User adminUser) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AcmException("Attendance session not found", HttpStatus.NOT_FOUND));

        if (session.getStatus() != AttendanceSessionStatus.ACTIVE) {
            throw new AcmException("Attendance session is not active", HttpStatus.BAD_REQUEST);
        }

        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            session.setStatus(AttendanceSessionStatus.EXPIRED);
            attendanceSessionRepository.save(session);
            throw new AcmException("Attendance session has expired", HttpStatus.BAD_REQUEST);
        }

        return attendanceTokenService.generateToken(session);
    }

    public SseEmitter streamRotatingTokens(Integer sessionId, User adminUser) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AcmException("Attendance session not found", HttpStatus.NOT_FOUND));

        if (session.getStatus() != AttendanceSessionStatus.ACTIVE) {
            throw new AcmException("Attendance session is not active", HttpStatus.BAD_REQUEST);
        }

        if (LocalDateTime.now().isAfter(session.getEndTime())) {
            session.setStatus(AttendanceSessionStatus.EXPIRED);
            attendanceSessionRepository.save(session);
            throw new AcmException("Attendance session has expired", HttpStatus.BAD_REQUEST);
        }

        long timeoutMillis = 30 * 60 * 1000L;
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Runnable pushTask = () -> {
            try {
                AttendanceSession current = attendanceSessionRepository.findById(sessionId).orElse(null);
                if (current == null || current.getStatus() != AttendanceSessionStatus.ACTIVE
                        || LocalDateTime.now().isAfter(current.getEndTime())) {
                    emitter.send(SseEmitter.event().name("session-stopped").data("Session is no longer active"));
                    emitter.complete();
                    executor.shutdown();
                    return;
                }

                AttendanceTokenResponse tokenResponse = attendanceTokenService.generateToken(current);
                emitter.send(SseEmitter.event().name("token").data(tokenResponse));
            } catch (Exception ex) {
                emitter.completeWithError(ex);
                executor.shutdown();
            }
        };

        pushTask.run();

        long intervalSeconds = session.getTokenExpirySeconds() != null && session.getTokenExpirySeconds() > 0
                ? session.getTokenExpirySeconds() : 5L;
        executor.scheduleAtFixedRate(pushTask, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);

        emitter.onCompletion(executor::shutdown);
        emitter.onTimeout(executor::shutdown);
        emitter.onError(t -> executor.shutdown());

        return emitter;
    }

    @Transactional
    public AttendanceRecordResponse markAttendanceByQr(String token, User currentUser) {
        if (currentUser == null) {
            throw new AcmException("Authentication required to mark attendance", HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(currentUser.getVerified())) {
            throw new AcmException("User account must be verified to mark attendance", HttpStatus.FORBIDDEN);
        }

        Integer sessionId = attendanceTokenService.extractSessionId(token);
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AcmException("Attendance session not found", HttpStatus.NOT_FOUND));

        long graceSeconds = session.getGracePeriodSeconds() != null ? session.getGracePeriodSeconds() : 5L;
        boolean pastGracePeriod = LocalDateTime.now().isAfter(session.getEndTime().plusSeconds(graceSeconds));

        if (session.getStatus() == AttendanceSessionStatus.STOPPED && pastGracePeriod) {
            throw new AcmException("Attendance session has been stopped by admin", HttpStatus.BAD_REQUEST);
        }

        if (pastGracePeriod) {
            if (session.getStatus() == AttendanceSessionStatus.ACTIVE) {
                session.setStatus(AttendanceSessionStatus.EXPIRED);
                attendanceSessionRepository.save(session);
            }
            throw new AcmException("Attendance session has expired", HttpStatus.BAD_REQUEST);
        }

        if (attendanceRecordRepository.existsByEventIdAndUserId(session.getEvent().getId(), currentUser.getId())) {
            throw new AcmException("Attendance already marked for this event", HttpStatus.CONFLICT);
        }

        boolean isValidToken = attendanceTokenService.validateToken(session, token);
        if (!isValidToken) {
            throw new AcmException("Invalid or expired attendance QR token", HttpStatus.BAD_REQUEST);
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .event(session.getEvent())
                .session(session)
                .user(currentUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.QR_SCAN)
                .xpAwarded(false)
                .build();

        try {
            AttendanceRecord saved = attendanceRecordRepository.save(record);
            return mapToRecordResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AcmException("Attendance already marked for this event", HttpStatus.CONFLICT);
        }
    }

    @Transactional
    public AttendanceRecordResponse markAttendanceManually(Integer sessionId, ManualAttendanceRequest request, User adminUser) {
        if (request == null || (request.getSid() == null && (request.getEmail() == null || request.getEmail().isBlank()))) {
            throw new AcmException("Student ID (SID) or email is required for manual check-in", HttpStatus.BAD_REQUEST);
        }

        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new AcmException("Attendance session not found", HttpStatus.NOT_FOUND));

        if (session.getEvent().isEnded()) {
            throw new AcmException("Cannot mark attendance for an ended event", HttpStatus.BAD_REQUEST);
        }

        User targetUser;
        if (request.getSid() != null) {
            if (request.getSid() < 10000000 || request.getSid() > 99999999) {
                throw new AcmException("Student ID (SID) must be an 8-digit number", HttpStatus.BAD_REQUEST);
            }
            targetUser = userRepository.findBySid(request.getSid())
                    .orElseThrow(() -> new AcmException("Student with SID " + request.getSid() + " not found", HttpStatus.NOT_FOUND));
        } else {
            targetUser = userRepository.findByEmail(request.getEmail().trim())
                    .orElseThrow(() -> new AcmException("Student with email " + request.getEmail() + " not found", HttpStatus.NOT_FOUND));
        }

        if (!Boolean.TRUE.equals(targetUser.getVerified())) {
            throw new AcmException("Student account is not verified", HttpStatus.BAD_REQUEST);
        }

        if (attendanceRecordRepository.existsByEventIdAndUserId(session.getEvent().getId(), targetUser.getId())) {
            throw new AcmException("Attendance already marked for this student", HttpStatus.CONFLICT);
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .event(session.getEvent())
                .session(session)
                .user(targetUser)
                .markedAt(LocalDateTime.now())
                .method(AttendanceMethod.MANUAL_ADMIN)
                .markedByAdminId(adminUser.getId())
                .xpAwarded(false)
                .build();

        try {
            AttendanceRecord saved = attendanceRecordRepository.save(record);
            return mapToRecordResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new AcmException("Attendance already marked for this student", HttpStatus.CONFLICT);
        }
    }

    public Page<AttendanceRecordResponse> getSessionAttendees(Integer sessionId, Pageable pageable) {
        if (!attendanceSessionRepository.existsById(sessionId)) {
            throw new AcmException("Attendance session not found", HttpStatus.NOT_FOUND);
        }

        return attendanceRecordRepository.findBySessionId(sessionId, pageable)
                .map(this::mapToRecordResponse);
    }

    public List<EventUserDetails> searchParticipants(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        List<User> users = userRepository.findAllBySearchQuery(query.trim(), true);
        return users.stream()
                .map(u -> new EventUserDetails(u.getId(), u.getEmail(), u.getName(), u.getDp()))
                .toList();
    }

    public List<String> getAttendeeEmailsForEvent(Integer eventId) {
        return attendanceRecordRepository.findByEventId(eventId).stream()
                .map(record -> record.getUser().getEmail())
                .distinct()
                .toList();
    }

    @Transactional
    public int awardAttendanceXp(Integer eventId, Integer xpAmount) {
        if (xpAmount != null && xpAmount <= 0) {
            throw new AcmException("XP amount must be greater than 0", HttpStatus.BAD_REQUEST);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new AcmException("Event not found", HttpStatus.NOT_FOUND));

        if (!event.isEnded()) {
            throw new AcmException("Cannot award attendance XP before the event has ended", HttpStatus.BAD_REQUEST);
        }

        List<AttendanceRecord> unAwarded = attendanceRecordRepository.findByEventIdAndXpAwardedFalse(eventId);
        if (unAwarded.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Transaction> transactions = new ArrayList<>();
        List<User> usersToUpdate = new ArrayList<>();

        for (AttendanceRecord record : unAwarded) {
            User user = record.getUser();
            record.setXpAwarded(true);

            if (!transactionRepository.existsByEventIdAndUserIdAndRole(eventId, user.getId(), EventRole.PARTICIPANT)) {
                int effectiveXp = (xpAmount != null && xpAmount > 0)
                        ? xpAmount
                        : (record.getSession() != null && record.getSession().getParticipantXp() != null
                        ? record.getSession().getParticipantXp() : 1);

                user.setXp(user.getXp() + effectiveXp);
                usersToUpdate.add(user);

                transactions.add(Transaction.builder()
                        .event(event)
                        .user(user)
                        .role(EventRole.PARTICIPANT)
                        .xp(effectiveXp)
                        .date(now)
                        .build());
            }
        }

        if (!transactions.isEmpty()) {
            transactionRepository.saveAll(transactions);
            userRepository.saveAll(usersToUpdate);
        }
        attendanceRecordRepository.saveAll(unAwarded);

        return unAwarded.size();
    }

    private AttendanceSessionResponse mapToSessionResponse(AttendanceSession session) {
        long totalAttendees = attendanceRecordRepository.countBySessionId(session.getId());
        return AttendanceSessionResponse.builder()
                .id(session.getId())
                .eventId(session.getEvent().getId())
                .eventTitle(session.getEvent().getTitle())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .status(session.getStatus())
                .createdById(session.getCreatedById())
                .tokenExpirySeconds(session.getTokenExpirySeconds())
                .gracePeriodSeconds(session.getGracePeriodSeconds())
                .participantXp(session.getParticipantXp())
                .totalAttendees(totalAttendees)
                .build();
    }

    private AttendanceRecordResponse mapToRecordResponse(AttendanceRecord record) {
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .eventId(record.getEvent().getId())
                .sessionId(record.getSession().getId())
                .userId(record.getUser().getId())
                .userName(record.getUser().getName())
                .userEmail(record.getUser().getEmail())
                .sid(record.getUser().getSid())
                .markedAt(record.getMarkedAt())
                .method(record.getMethod())
                .markedByAdminId(record.getMarkedByAdminId())
                .xpAwarded(record.getXpAwarded())
                .build();
    }
}
