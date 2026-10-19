package com.pecacm.backend.repository;

import com.pecacm.backend.entities.AttendanceSession;
import com.pecacm.backend.enums.AttendanceSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Integer> {

    Optional<AttendanceSession> findFirstByEventIdAndStatusOrderByStartTimeDesc(Integer eventId, AttendanceSessionStatus status);

    List<AttendanceSession> findByEventIdOrderByStartTimeDesc(Integer eventId);

    List<AttendanceSession> findAllByStatusAndEndTimeBefore(AttendanceSessionStatus status, LocalDateTime now);

    Optional<AttendanceSession> findByIdAndEventId(Integer id, Integer eventId);
}
