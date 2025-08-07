package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.PostType;
import lombok.Getter;
import lombok.Setter;

/**
 * 게시글(posts) 생성 및 수정 시 전달되는 데이터 전송 객체(DTO)
 * <ul>
 *     <li>기본 게시글 정보 : 작성자 id, 유형(type), 제목(title), 내용(content) </li>
 *     <lI>1:1 매핑 엔티티 정보를 각각의 하위 DTO로 포함 (teamInfo, jobInfo, baseInfo, hangoutInfo)</lI>
 * </ul>
 */

@Getter
@Setter
public class PostDTO {

    private Long userId;
    private PostType type;
    private String title;
    private String content;
    private PostTeamInformationDTO teamInfo;
    private PostJobDTO jobInfo;
    private PostTagsDTO tagsInfo;
    private PostHangoutDTO hangoutInfo;
    private PostQnADTO qnaInfo;
}