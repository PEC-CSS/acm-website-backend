package com.pecacm.backend.response;

import com.pecacm.backend.entities.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SupportEventResponse {
    private Event event;
    private Page<EventUserDetails> participants;
    private List<EventUserDetails> contributors;
    private List<EventUserDetails> publicity;
}
