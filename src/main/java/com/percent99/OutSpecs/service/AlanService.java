package com.percent99.OutSpecs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class AlanService {
  private final RestTemplate restTemplate;

  @Value("${alan.BASE_URL}")
  private String baseUrl;

  @Value("${alan.CLIENT_ID}")
  private String alanClientId;

  private String sendRequest(String content){
    String url = UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("content", content)
            .queryParam("client_id", alanClientId)
            .build()
            .toUriString();

    return restTemplate.getForObject(url, String.class);
  }

  public String getRecommend(String placeName){
    String content = String.format("%s 지역의 명소를 5곳, 맛집을 5곳 추천", placeName);

    return this.sendRequest(content);
  }

  public String getAnswer(String question){
    return this.sendRequest(question);
  }
}
