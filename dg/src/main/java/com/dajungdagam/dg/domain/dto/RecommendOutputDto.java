package com.dajungdagam.dg.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecommendOutputDto {

    private int userId;

    private List<String> postTitles;

    public RecommendOutputDto(int userId, List<String> postTitles) {
        this.userId = userId;
        this.postTitles = postTitles;
    }
}