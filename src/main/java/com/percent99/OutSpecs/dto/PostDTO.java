package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "유저 ID는 필수입니다.")
    private Long userId;
    @NotNull(message = "타입은 필수입니다.")
    private PostType type;
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    private PostTeamInformationDTO teamInfo;
    private PostJobDTO jobInfo;
    private PostTagsDTO tagsInfo;
    private PostHangoutDTO hangoutInfo;
    private PostQnADTO qnaInfo;
}