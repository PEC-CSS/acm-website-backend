package com.pecacm.backend.response;

import com.pecacm.backend.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;


@Getter
@Setter
@AllArgsConstructor
public class SupportUserResponse {
    private User user;
    private Page<UserEventDetails> events;
}
