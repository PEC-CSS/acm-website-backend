package com.pecacm.backend.services;

import com.pecacm.backend.constants.ErrorConstants;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.repository.AttendanceRepository;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventService(EventRepository eventRepository, AttendanceRepository attendanceRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    // TODO : change all GET events to pageable repositories

    public List<Event> getAllEvents() {
        return new ArrayList<>(eventRepository.findAll());
    }

    public Event getSingleEvent(Integer eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new AcmException("Event doesn't not exist with id :" + eventId, HttpStatus.NOT_FOUND);
        }
        return event.get();
    }

    public List<Event> getEventsByBranch(String branch) {
        return eventRepository.findByBranch(branch);
    }

    public List<Event> getUserEvents(Integer userId) {
        List<Event> events = new ArrayList<>();
        attendanceRepository.findByUserId(userId).forEach(
                attendance -> events.add(attendance.getEvent())
        );
        return events;
    }

    public List<Event> getUserEventsByRole(Integer userId, String role) {
        Optional<User> user = userRepository.findById(userId);
        List<Event> events = new ArrayList<>();
        if (user.isEmpty()) {
            throw new AcmException(ErrorConstants.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        attendanceRepository.findByUserIdAndRole(userId, role).forEach(
                attendance -> events.add(attendance.getEvent())
        );
        return events;
    }

    public Event createEvent(Event event) {
        try {
            return eventRepository.save(event);
        } catch (Exception e) {
            throw new AcmException("Event not created, " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Event updateEvent(Event updatedEvent, Integer eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty() || !Objects.equals(event.get().getId(), eventId)) {
            throw new AcmException("Event cannot be updated", HttpStatus.BAD_REQUEST);
        }
        updatedEvent.setId(eventId);
        return eventRepository.save(updatedEvent);
    }

    public void deleteEvent(Integer eventId) {
        try {
            eventRepository.deleteById(eventId);
        } catch (Exception ex) {
            throw new AcmException("Event cannot be deleted, eventId=" + eventId, HttpStatus.BAD_REQUEST);
        }
    }
}
