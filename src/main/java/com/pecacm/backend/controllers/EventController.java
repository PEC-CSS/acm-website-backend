package com.pecacm.backend.controllers;

import com.pecacm.backend.entities.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pecacm.backend.services.EventService;

import java.util.List;
import java.util.Optional;

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
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Event>> getUserEvents(@PathVariable Integer userId){
        return ResponseEntity.ok(eventService.getUserEvents(userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Event>> getUserEventsByRole(@PathVariable Integer userId, @RequestParam("role") String role) {
        List<Event> events = eventService.getUserEventsByRole(userId, role);
        return ResponseEntity.ok(events);
    }
     @GetMapping("/{eventId}")
     public ResponseEntity<Optional<Event>> getSingleEvent(@PathVariable Integer eventId){
        // TODO: Status theek lgao
        return ResponseEntity.ok(eventService.getSingleEvent(eventId));
    }

    @GetMapping("/branches/{branch}")
    public ResponseEntity<List<Event>> getEventsByBranch(@PathVariable String branch){
        List<Event> events = eventService.getEventsByBranch(branch);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<Void> createEvent(@RequestBody Event event){
        if (eventService.createEvent(event)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    @PutMapping("/{eventId}/end")
//    public ResponseEntity<Event> endEvent(@RequestBody Event event){
//        Event updatedEvent = eventService.endEvent(event);
//        return ResponseEntity.ok(updatedEvent);
//    }

    @PutMapping
    public ResponseEntity<Event> updateEvent(@RequestBody Event event){
        Event updatedEvent = eventService.updateEvent(event);
        if (updatedEvent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer eventId) {
        Boolean successfullyDeleted = eventService.deleteEvent(eventId);
        return (successfullyDeleted) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
