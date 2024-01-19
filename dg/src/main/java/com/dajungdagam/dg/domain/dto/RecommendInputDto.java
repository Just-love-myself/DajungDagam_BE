package com.dajungdagam.dg.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecommendInputDto {
    private int user_id;
    private int post_id;
    private String post_title;

    //찜 했는지 여부 (1 or 0), 임의로 만들어서 넣을 것.
    private int interaction;

    private int num_recommendations;

    public RecommendInputDto(int user_id, int post_id, String post_title, int interaction, int num_recommendations) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.post_title = post_title;
        this.interaction = interaction;
        this.num_recommendations = num_recommendations;
    }
}