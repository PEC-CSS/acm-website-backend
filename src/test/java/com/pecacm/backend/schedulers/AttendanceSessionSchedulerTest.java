package com.pecacm.backend.schedulers;

import com.pecacm.backend.services.AttendanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttendanceSessionSchedulerTest {

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceSessionScheduler scheduler;

    @Test
    void testAutoExpireStaleSessions() {
        scheduler.autoExpireStaleSessions();

        verify(attendanceService, times(1)).expireStaleSessions();
    }
}
