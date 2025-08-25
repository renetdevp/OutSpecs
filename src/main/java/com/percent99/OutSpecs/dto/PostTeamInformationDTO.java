package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.PostStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * post_team_information 테이블 매핑용 DTO.
 * <p>팀 모집 게시판에서 사용되는 모집 상태와 최대 인원을 캡슐화</p>
 */
@Getter
@Setter
public class PostTeamInformationDTO {

    private PostStatus status = PostStatus.OPEN;
    private Integer capacity;
}