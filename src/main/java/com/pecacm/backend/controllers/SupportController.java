package com.pecacm.backend.controllers;

import com.pecacm.backend.constants.Constants;
import com.pecacm.backend.entities.Event;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.response.EventUserDetails;
import com.pecacm.backend.response.SupportEventResponse;
import com.pecacm.backend.response.SupportUserResponse;
import com.pecacm.backend.services.EventService;
import com.pecacm.backend.services.SupportService;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/support")
public class SupportController {

    private final SupportService supportService;
    private final EventService eventService;

    public SupportController(SupportService supportService, EventService eventService) {
        this.supportService = supportService;
        this.eventService = eventService;
    }

    @GetMapping("/user/{email}")
    @PreAuthorize(Constants.HAS_ROLE_ADMIN)
    public ResponseEntity<SupportUserResponse>  getUserDetails(@PathVariable String email, @RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {

        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 20;

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be >= 0", HttpStatus.BAD_REQUEST);

        return ResponseEntity.ok(supportService.getSupportUserDetails(email, PageRequest.of(offset, pageSize)));
    }

    @GetMapping("/events/{eventId}")
    @PreAuthorize(Constants.HAS_ROLE_ADMIN)
    public ResponseEntity<SupportEventResponse> getEventDetails(@PathVariable Integer eventId, @RequestParam @Nullable Integer offset, @RequestParam @Nullable Integer pageSize) {

        if (offset == null) offset = 0;
        if (pageSize == null) pageSize = 10;

        if (offset < 0) throw new AcmException("offset cannot be < 0", HttpStatus.BAD_REQUEST);
        if (pageSize <= 0) throw new AcmException("pageSize must be >= 0", HttpStatus.BAD_REQUEST);

        Event event = eventService.getSingleEvent(eventId);
        Pair<List<EventUserDetails>, List<EventUserDetails>> nonParticipants = supportService.getNonParticipants(eventId);
        Page<EventUserDetails> participants = supportService.getEventParticipants(event, PageRequest.of(offset, pageSize));

        return ResponseEntity.ok(new SupportEventResponse(event, participants, nonParticipants.getFirst(), nonParticipants.getSecond()));
    }
}
