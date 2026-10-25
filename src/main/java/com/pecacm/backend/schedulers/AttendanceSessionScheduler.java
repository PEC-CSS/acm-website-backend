package com.pecacm.backend.schedulers;

import com.pecacm.backend.services.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AttendanceSessionScheduler {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceSessionScheduler(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(fixedDelay = 60000)
    public void autoExpireStaleSessions() {
        attendanceService.expireStaleSessions();
    }
}
