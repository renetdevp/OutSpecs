package com.percent99.OutSpecs.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * post_qna 테이블 매핑용 DTO.
 * <p>qna 게시판에서 사용되는 답변완료상태를 캡슐화</p>
 */

@Getter
@Setter
public class PostQnADTO {
    private Boolean answerComplete;
}
