package com.pecacm.backend.repository;

import com.pecacm.backend.entities.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {

    boolean existsByEventIdAndUserId(Integer eventId, Integer userId);

    boolean existsBySessionIdAndUserId(Integer sessionId, Integer userId);

    Optional<AttendanceRecord> findBySessionIdAndUserId(Integer sessionId, Integer userId);

    List<AttendanceRecord> findBySessionId(Integer sessionId);

    Page<AttendanceRecord> findBySessionId(Integer sessionId, Pageable pageable);

    long countBySessionId(Integer sessionId);

    List<AttendanceRecord> findByEventId(Integer eventId);

    List<AttendanceRecord> findByEventIdAndXpAwardedFalse(Integer eventId);
}
