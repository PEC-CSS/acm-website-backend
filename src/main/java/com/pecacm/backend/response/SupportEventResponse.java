package com.pecacm.backend.response;

import com.pecacm.backend.entities.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SupportEventResponse {
    private Event event;
    private EventAttendeesResponse eventAttendeesResponse;
}
