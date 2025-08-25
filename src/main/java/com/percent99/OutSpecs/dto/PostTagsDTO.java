package com.percent99.OutSpecs.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * post_tags 테이블 매핑된 DTO
 * <p>일반 게시판(Q&A, FREE) 에서 사용되는 태그를 캡슐화</p>
 */
@Getter
@Setter
public class PostTagsDTO {
    private String tags;
}