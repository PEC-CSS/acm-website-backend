package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByUserId(Integer id);
    List<Attendance> findByUserIdAndRole(Integer id, String role);
}