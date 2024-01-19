package com.dajungdagam.dg.service;

import com.dajungdagam.dg.domain.dto.RecommendInputDto;
import com.dajungdagam.dg.domain.dto.RecommendOutputDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

    private final WebClient webClient;


    public WebClientService(@Value("${fastapi.server.url}") String fastApiServerUrl) {
        this.webClient = WebClient.create(fastApiServerUrl);
    }

    public RecommendOutputDto sendHttpPostRequestToFastApi(RecommendInputDto recommendInputDto) {
        return webClient.post()
                .uri("/get_recommendations")  // FastAPI 서버의 엔드포인트에 맞게 수정
                //Dto requestBody에 담기
                .body(BodyInserters.fromValue(recommendInputDto))
                .retrieve()
                .bodyToMono(RecommendOutputDto.class)
                .block();
    }
}