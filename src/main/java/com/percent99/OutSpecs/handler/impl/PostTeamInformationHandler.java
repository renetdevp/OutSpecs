package com.percent99.OutSpecs.handler.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostTeamInformation;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import org.springframework.stereotype.Component;

/**
 * TEAM 모집 (posts_team_information) 상세 정보 설정 구현체
 */
@Component
public class PostTeamInformationHandler implements PostDetailHandler{

    @Override
    public boolean supports(PostType type) {
        return type == PostType.TEAM;
    }

    @Override
    public void handle(Post post, PostDTO dto) {
        if(dto.getTeamInfo() == null) return;
        PostTeamInformation teamInfo = new PostTeamInformation();
        teamInfo.setPost(post);
        teamInfo.setStatus(dto.getTeamInfo().getStatus());
        teamInfo.setCapacity(dto.getTeamInfo().getCapacity());
        post.setTeamInfo(teamInfo);
    }
}