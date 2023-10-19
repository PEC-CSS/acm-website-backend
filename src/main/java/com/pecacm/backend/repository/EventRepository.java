package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Event;
import com.pecacm.backend.enums.Branch;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByBranch(Branch branch,PageRequest pageRequest);
    List<Event> findAllByEndedFalse(PageRequest pageRequest);
    @Query("SELECT e from Event e WHERE e.startDate > :currDateTime ORDER BY e.startDate ASC LIMIT 1")
    Event getNearestEvent(@Param("currDateTime") LocalDateTime currDate);
    List<Event> findAllByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDateTime startDate, LocalDateTime endDate, PageRequest pageRequest);
}