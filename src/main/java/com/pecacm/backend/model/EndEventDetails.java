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
    @Builder.Default
    private List<String> contributors = new ArrayList<>();
    @Builder.Default
    private List<String> publicity = new ArrayList<>();
    // Can take gsheet link instead and call gsheet api
    @Builder.Default
    private List<String> participants = new ArrayList<>();
    @Builder.Default
    private Integer contributorXp = 5;
    @Builder.Default
    private Integer publicityXp = 2;
    @Builder.Default
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
