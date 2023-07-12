package com.pecacm.backend.repository;

import com.pecacm.backend.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository <Event, Integer> {
    List<Event> findByBranch(String branch);
}
