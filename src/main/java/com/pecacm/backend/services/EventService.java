package com.pecacm.backend.services;

import com.pecacm.backend.entities.Event;
import com.pecacm.backend.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    // TODO : change all GET events to pageable repositories

    public List<Event> getAllEvents() {
        return new ArrayList<>(eventRepository.findAll());
    }

    public Optional<Event> getSingleEvent(Integer eventId) {
        return eventRepository.findById(eventId);
    }

    public List<Event> getEventsByBranch(String branch) {
        return eventRepository.findByBranch(branch);
    }

    public Integer createEvent(Event event) {
        eventRepository.save(event);
        return event.getId();
    }

    public Event updateEvent(Event updatedEvent, Integer eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if(event.isEmpty() || !Objects.equals(event.get().getId(), eventId)) {
            return null;
        }
        return eventRepository.save(updatedEvent);
    }

    public void deleteEvent(Integer eventId) {
        eventRepository.deleteById(eventId);
    }
}
