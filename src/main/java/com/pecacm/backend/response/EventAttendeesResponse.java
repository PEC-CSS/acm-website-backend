package com.pecacm.backend.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class EventAttendeesResponse {
    private List<EventUserDetails> contributors;
    private List<EventUserDetails> publicity;
    private List<EventUserDetails> participants;
}
