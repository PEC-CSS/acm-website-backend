package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.enums.Branch;
import com.pecacm.backend.enums.EventRole;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.EndEventDetails;
import com.pecacm.backend.services.EventService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<List<Event>> getAllEvents(@RequestParam @Nullable LocalDate eventsFrom, @RequestParam @Nullable LocalDate eventsTill) {

        if (eventsFrom==null){
            eventsFrom = LocalDate.now().minusYears(99);
        }
        if (eventsTill==null){
            eventsTill = LocalDate.now();
        }

        if (eventsFrom.isAfter(eventsTill)) {
            throw new AcmException("eventsFrom Date must be <= eventsTill Date", HttpStatus.BAD_REQUEST);
        }
        // TODO : Return pageable response

        List<Event> events = eventService.getEventsBetweenTwoTimestamps(eventsFrom, eventsTill);

        return ResponseEntity.ok(events);
    }

    @GetMapping("/ongoing")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<List<Event>> getOngoingEvents() {
        return ResponseEntity.ok(eventService.getOngoingEvents());
    }

    @GetMapping("/{eventId}")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<Event> getSingleEvent(@PathVariable Integer eventId){
        // TODO : Return pageable response
        Event event = eventService.getSingleEvent(eventId);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/branches/{branch}")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<List<Event>> getEventsByBranch(@PathVariable Branch branch){
        // TODO : Return pageable response
        List<Event> events = eventService.getEventsByBranch(branch);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize(Constants.HAS_ROLE_MEMBER_AND_ABOVE)
    public ResponseEntity<List<Event>> getUserEventsByRole(@PathVariable Integer userId, @RequestParam("role") @Nullable EventRole eventRole) {
        if (eventRole == null){
            return ResponseEntity.ok(eventService.getUserEventsByRole(userId, EventRole.PARTICIPANT));
        }
        return ResponseEntity.ok(eventService.getUserEventsByRole(userId, eventRole));
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

    @PostMapping("/{eventId}/end")
    @PreAuthorize(Constants.HAS_ROLE_CORE_AND_ABOVE)
    public ResponseEntity<Void> endEvent(@PathVariable Integer eventId, @RequestBody EndEventDetails endEventDetails) {
        eventService.endEvent(eventId, endEventDetails);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/next")
    @PreAuthorize(Constants.HAS_ANY_ROLE)
    public ResponseEntity<Event> nextEvent(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Nonnull LocalDate date) {
        LocalDateTime currDateTime = LocalDateTime.of(date, LocalTime.of(0, 0));
        Event nextEvent = eventService.getNextEvent(currDateTime);
        return ResponseEntity.ok(nextEvent);
    }
}
