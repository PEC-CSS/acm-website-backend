package com.pecacm.backend.model;

import com.pecacm.backend.enums.EventRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EndEventDetails {
    private List<String> contributors = new ArrayList<>();
    private List<String> publicity = new ArrayList<>();
    // Can take gsheet link instead and call gsheet api
    private List<String> participants = new ArrayList<>();
    private Integer contributorXp = 5;
    private Integer publicityXp = 2;
    private Integer participantXp = 1;

    public Integer getXp(EventRole role) {
        switch (role) {
            case ORGANIZER -> {
                return contributorXp;
            }
            case PUBLICITY -> {
                return publicityXp;
            }
            default -> {
                return participantXp;
            }
        }
    }
}
