package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/events")
public class EventsController {

    private final EventService eventService;

    @Autowired
    public EventsController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<List<Event>> getAllEvents() {
        // TODO : Return pageable response
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<Event> getSingleEvent(@PathVariable Integer eventId){
        // TODO : Return pageable response
        Event event = eventService.getSingleEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/branches/{branch}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<List<Event>> getEventsByBranch(@PathVariable String branch){
        // TODO : Return pageable response
        List<Event> events = eventService.getEventsByBranch(branch);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<List<Event>> getUserEventsByRole(@PathVariable Integer userId, @RequestParam("role") String role) {
        if (role == null){
            return ResponseEntity.ok(eventService.getUserEvents(userId));
        }
        return ResponseEntity.ok(eventService.getUserEventsByRole(userId, role));
    }

    @PostMapping
    @PreAuthorize(Constants.HAS_ROLE_IB_AND_ABOVE)
    public ResponseEntity<Event> createEvent(@RequestBody Event event){
        Event createdEvent = eventService.createEvent(event);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize(Constants.HAS_ROLE_EB_AND_ABOVE)
    public ResponseEntity<Event> updateEvent(@RequestBody Event event, @PathVariable Integer eventId){
        Event updatedEvent = eventService.updateEvent(event, eventId);
        return (updatedEvent == null) ? ResponseEntity.badRequest().build() : ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
