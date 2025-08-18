package com.percent99.OutSpecs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.percent99.OutSpecs.entity.AlanQuestionType;
import com.percent99.OutSpecs.exception.HttpResponseProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 이스트소프트의 Alan AI에게 질의하기 위한 서비스.<br>
 * '놀러 나가기' 기능을 위한 getRecommend() 메소드와 챗봇 기능을 위한 getAnswer() 메소드가 존재.
 */
@RequiredArgsConstructor
@Service
public class AlanService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final UserService userService;

  @Value("${alan.BASE_URL}")
  private String baseUrl;

  @Value("${alan.CLIENT_ID}")
  private String alanClientId;

  /**
   * 앨런 AI에게 질의하고 그 응답을 받아 파싱하는 메소드
   * @param content 앨런 AI에게 질의할 질문의 내용
   * @return 앨런 AI에게 질의해 받은 응답을 파싱한 내용
   */
  private Map<String, String> sendRequest(String content, Long userId){
    if (userService.getUserById(userId).getAiRateLimit() <= 0) return null;

    String url = UriComponentsBuilder.fromUriString(baseUrl)
            .queryParam("content", content)
            .queryParam("client_id", alanClientId)
            .build()
            .toUriString();

    ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);

    try {
      Map<String, String> result = new HashMap<>();
      String response = objectMapper.readTree(res.getBody()).get("content").asText();

      result.put("response", response);
      userService.decrementAiRateLimit(userId);

      return result;
    } catch (JsonProcessingException e) {
      throw new HttpResponseProcessingException(e);
    }
  }

  private Map<String, String> getRecommend(String placeName, Long userId){
    String content = String.format("%s 지역의 명소를 5곳, 맛집을 5곳 추천", placeName);

    return this.sendRequest(content, userId);
  }

  private Map<String, String> getAnswer(String question, Long userId){
    return this.sendRequest(question, userId);
  }

  /**
   * 사용자가 앨런 AI에게 던진 질의의 종류에 따라 해당 질의를 처리하는 핸들러 메소드
   * @param questionType 사용자가 앨런 AI에게 던진 질의의 종류
   * @param question 사용자가 앨런 AI에게 던진 질의의 내용
   * @param userId 앨런 AI에게 질의하는 사용자의 id 값
   * @return 사용자가 앨런 AI에게 던진 질의에 대한 앨런 AI의 답변 내용.
   */
  public Map<String, String> question(String questionType, String question, Long userId){
    if (questionType==null || question==null || userId==null) return null;

    if (AlanQuestionType.RECOMMEND.name().equals(questionType)) return getRecommend(question, userId);
    else if (AlanQuestionType.QUESTION.name().equals(questionType)) return getAnswer(question, userId);

    return null;
  }
}
