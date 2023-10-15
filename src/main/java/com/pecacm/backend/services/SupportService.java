package com.pecacm.backend.services;

import com.pecacm.backend.entities.Event;
import com.pecacm.backend.entities.Transaction;
import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.EventRole;
import com.pecacm.backend.repository.TransactionRepository;
import com.pecacm.backend.response.EventUserDetails;
import com.pecacm.backend.response.UserEventDetails;
import com.pecacm.backend.response.SupportUserResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupportService {
    private final TransactionRepository transactionRepository;

    public SupportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public SupportUserResponse getSupportUserDetails(User user, PageRequest pageRequest) {
        Page<Transaction> transactions = transactionRepository.findByUserId(user.getId(), pageRequest);
        List<UserEventDetails> events = new ArrayList<>();
        transactions.forEach(transaction -> {
            Event event = transaction.getEvent();
            UserEventDetails userEventDetails = new UserEventDetails(event.getId(), event.getTitle(), transaction.getRole(), transaction.getXp(), event.getEndDate());
            events.add(userEventDetails);
        });
        Page<UserEventDetails> eventsPage = new PageImpl<>(events, pageRequest, events.size());
        return new SupportUserResponse(user, eventsPage);
    }

    @Cacheable("getNonParticipants")
    public Pair<List<EventUserDetails>, List<EventUserDetails>> getNonParticipants(Integer eventId) {
        List<EventUserDetails> contributors = new ArrayList<>();
        List<EventUserDetails> publicity = new ArrayList<>();

        transactionRepository.findListByEventIdAndRoles(eventId, List.of(EventRole.ORGANIZER, EventRole.PUBLICITY)).forEach(transaction -> {
            User user = transaction.getUser();
            EventUserDetails eventUserDetails = new EventUserDetails(user.getId(), user.getEmail(), user.getName(), user.getDp());
            if (transaction.getRole() == EventRole.ORGANIZER) {
                contributors.add(eventUserDetails);
            } else if (transaction.getRole() == EventRole.PUBLICITY) {
                publicity.add(eventUserDetails);
            }
        });

        return Pair.of(contributors, publicity);
    }

    public Page<EventUserDetails> getEventParticipants(Event event, PageRequest pageRequest) {
        List<EventUserDetails> participants = new ArrayList<>();
        transactionRepository.findByEventIdAndRole(event.getId(), EventRole.PARTICIPANT, pageRequest).forEach(transaction -> {
            User user = transaction.getUser();
            EventUserDetails eventUserDetails = new EventUserDetails(user.getId(), user.getEmail(), user.getName(), user.getDp());
            participants.add(eventUserDetails);
        });

        return new PageImpl<>(participants, pageRequest, participants.size());
    }
}
