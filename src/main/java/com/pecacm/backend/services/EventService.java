package com.pecacm.backend.services;

import com.pecacm.backend.entities.Event;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private EventRepository eventRepository;
    private AttendanceRepository attendanceRepository;
    @Autowired
    public EventService(EventRepository eventRepository, AttendanceRepository attendanceRepository) {
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
    }

    public List<Event> getAllEvents() {
        List <Event> events = new ArrayList<>();
        eventRepository.findAll().forEach(events::add);
        return events;
    }

    public List<Event> getUserEvents(Integer userId) {
        List <Event> events = new ArrayList<>();
        attendanceRepository.findByUserId(userId).forEach(
                attendance -> events.add(attendance.getEvent())
        );
        return events;
    }

    public List<Event> getUserEventsByRole(Integer userId, String role) {
        List <Event> events = new ArrayList<>();
        attendanceRepository.findByUserIdAndRole(userId, role).forEach(
                attendance -> events.add(attendance.getEvent())
        );
        return events;
    }

    public Boolean createEvent(Event event) {
        // TODO: Check if there's an event clashing with it idk
        eventRepository.save(event);
        return true;
    }

    public Optional<Event> getSingleEvent(Integer eventId) {
        return eventRepository.findById(eventId);
    }

    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    public void deleteEvent(Integer eventId) {
        eventRepository.deleteById(eventId);
    }

    public List<Event> getEventsByBranch(String branch) {
        return eventRepository.findByBranch(branch);
    }

}
