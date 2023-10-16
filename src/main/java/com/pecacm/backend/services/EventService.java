package com.pecacm.backend.services;

import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.Branch;
import com.pecacm.backend.enums.EventRole;
import com.pecacm.backend.exception.AcmException;
import com.pecacm.backend.model.EndEventDetails;
import com.pecacm.backend.repository.EventRepository;
import com.pecacm.backend.repository.TransactionRepository;
import com.pecacm.backend.repository.UserRepository;
import com.pecacm.backend.response.EventAttendeesResponse;
import com.pecacm.backend.response.EventUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SupportService supportService;

    public EventService(EventRepository eventRepository, UserRepository userRepository, TransactionRepository transactionRepository, SupportService supportService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.supportService = supportService;
    }

    // TODO : change all GET events to pageable repositories

    public List<Event> getEventsBetweenTwoTimestamps(LocalDate eventsFrom, LocalDate eventsTill) {
        return eventRepository
                .findAllByStartDateGreaterThanEqualAndEndDateLessThanEqual(
                        eventsFrom.atStartOfDay(),
                        eventsTill.plusDays(1).atStartOfDay()
                );
    }

    public List<Event> getOngoingEvents() {
        return eventRepository.findAllByEndedFalse();
    }

    public Event getSingleEvent(Integer eventId) {
        Optional<Event> event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            throw new AcmException("Event doesn't not exist with id :" + eventId, HttpStatus.NOT_FOUND);
        }
        return event.get();
    }

    public List<Event> getEventsByBranch(Branch branch) {
        return eventRepository.findByBranch(branch);
    }

    public EventAttendeesResponse getEventUsersByRole(Integer eventId, EventRole eventRole, PageRequest pageRequest) {
        if (eventRole == null) {
            Pair<List<EventUserDetails>, List<EventUserDetails>> nonParticipants = supportService.getNonParticipants(eventId);
            List<EventUserDetails> participants = supportService.getEventParticipants(eventId, pageRequest);
            return new EventAttendeesResponse(nonParticipants.getFirst(), nonParticipants.getSecond(), participants);
        } else {
            List<EventUserDetails> attendees = transactionRepository.findByEventIdAndRole(eventId, eventRole, pageRequest)
                    .stream().map(transaction -> {
                        User user = transaction.getUser();
                        return new EventUserDetails(user.getId(), user.getEmail(), user.getName(), user.getDp());
                    }).toList();

            return switch (eventRole) {
                case ORGANIZER -> new EventAttendeesResponse(attendees, new ArrayList<>(), new ArrayList<>());
                case PUBLICITY -> new EventAttendeesResponse(new ArrayList<>(), attendees, new ArrayList<>());
                case PARTICIPANT -> new EventAttendeesResponse(new ArrayList<>(), new ArrayList<>(), attendees);
            };
        }
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

    @Transactional
    public void endEvent(Integer eventId, EndEventDetails endEventDetails) {

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new AcmException("Event not found", HttpStatus.NOT_FOUND));

        if (event.isEnded()) {
            throw new AcmException("Event has already ended", HttpStatus.BAD_REQUEST);
        }

        Map<String, User> userMap = new HashMap<>();
        Set<String> emails = new HashSet<>();

        emails.addAll(endEventDetails.getContributors());
        emails.addAll(endEventDetails.getPublicity());
        emails.addAll(endEventDetails.getParticipants());

        List<User> users = userRepository.findAllByEmail(emails);
        users.forEach(user -> {
            userMap.put(user.getEmail(), user);
        });

        List<Pair<User, EventRole>> eventUsers = new ArrayList<>();

        endEventDetails.getContributors().forEach(email -> {
            Optional.ofNullable(userMap.get(email)).ifPresent(user -> {
                eventUsers.add(Pair.of(user, EventRole.ORGANIZER));
            });
        });
        endEventDetails.getPublicity().forEach(email -> {
            Optional.ofNullable(userMap.get(email)).ifPresent(user -> {
                eventUsers.add(Pair.of(user, EventRole.PUBLICITY));
            });
        });
        endEventDetails.getParticipants().forEach(email -> {
            Optional.ofNullable(userMap.get(email)).ifPresent(user -> {
                eventUsers.add(Pair.of(user, EventRole.PARTICIPANT));
            });
        });

        List<Transaction> transactions = eventUsers.stream().map((userEventRolePair) -> {
            User user = userEventRolePair.getFirst();
            EventRole eventRole = userEventRolePair.getSecond();
            Integer xp = endEventDetails.getXp(eventRole);

            user.setXp(user.getXp() + xp);

            return Transaction.builder()
                    .event(event)
                    .xp(xp)
                    .user(user)
                    .role(eventRole)
                    .date(LocalDateTime.now())
                    .build();
        }).toList();

        transactionRepository.saveAll(transactions);
        userRepository.saveAll(users);

        event.setEnded(true);
        eventRepository.save(event);

    }

    public Event getNextEvent(LocalDateTime currDateTime) {
        return eventRepository.getNearestEvent(currDateTime);
    }
}
