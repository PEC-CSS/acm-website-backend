package com.pecacm.backend.controllers;

import com.pecacm.backend.entities.Event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pecacm.backend.services.EventService;

import java.util.List;

@RestController
@RequestMapping("v1/events")
public class EventController {
    private EventService eventService;
    @Autowired
    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents(){
        List<Event> events = eventService.getAllEvents();
        if (events.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(events);
    }

     @GetMapping("/{eventId}")
     public ResponseEntity<Event> getSingleEvent(@PathVariable Integer eventId){
         Event event = eventService.getSingleEvent(eventId)
                 .orElseThrow(() -> new ResourceNotFoundException("Event doesn't not exist with id :" + eventId));
        return ResponseEntity.ok(event);
    }

    // TODO: Ask if there's a need to handle empty list exceptions everywhere
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Event>> getUserEventsByRole(@PathVariable Integer userId, @RequestParam("role") String role) {
        if (role == null){
            return ResponseEntity.ok(eventService.getUserEvents(userId));
        }
        return ResponseEntity.ok(eventService.getUserEventsByRole(userId, role));
    }

    @GetMapping("/branches/{branch}")
    public ResponseEntity<List<Event>> getEventsByBranch(@PathVariable String branch){
        List<Event> events = eventService.getEventsByBranch(branch);
        return ResponseEntity.ok(events);
    }

    // TODO: Handle business logic in case of an already created event during the dates
    // Need to return the created event?
    @PostMapping
    public ResponseEntity<Void> createEvent(@RequestBody Event event){
        if (!eventService.createEvent(event)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // TODO: Handle business logic in case of clashing events
    //  Handle Clash exception and no event found separately.
    @PutMapping("/{eventId}")
    public ResponseEntity<Event> updateEvent(@RequestBody Event event, @PathVariable String eventId){
        Event updatedEvent = eventService.updateEvent(event);
        return (updatedEvent == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(updatedEvent);
    }

    // Silent deletion
    // TODO: Ask if its needed to handle if event does not exist
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
