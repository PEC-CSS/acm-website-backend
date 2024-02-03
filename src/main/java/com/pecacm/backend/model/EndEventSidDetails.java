package com.pecacm.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EndEventSidDetails {
    private List<String> contributors = new ArrayList<>();
    private List<String> publicity = new ArrayList<>();
    private List<Integer> participants = new ArrayList<>();
    private Integer contributorXp = 5;
    private Integer publicityXp = 2;
    private Integer participantXp = 1;
}
