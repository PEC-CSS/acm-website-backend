package com.pecacm.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// shared by questions and answers, both of which only carry content. The question
// an answer belongs to comes from the path, not the body
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QnaRequest {
    private String content;
}
