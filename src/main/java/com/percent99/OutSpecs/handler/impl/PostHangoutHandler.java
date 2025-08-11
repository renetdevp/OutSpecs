package com.percent99.OutSpecs.handler.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostHangout;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import org.springframework.stereotype.Component;

/**
 * 나가서 놀기(posts_hangout) 상세 정보 설정 구현체
 */
@Component
public class PostHangoutHandler implements PostDetailHandler {

    @Override
    public boolean supports(PostType type) {
        return type == PostType.PLAY;
    }

    @Override
    public void handle(Post post, PostDTO dto) {
        if(dto.getHangoutInfo() == null) return;

        PostHangout postHangout = post.getPostHangout();
        if(postHangout == null){
            postHangout = new PostHangout();
            postHangout.setPost(post);
            post.setPostHangout(postHangout);
        }
        postHangout.setPlaceName(dto.getHangoutInfo().getPlaceName());
    }
}