package com.percent99.OutSpecs.handler.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostBase;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import org.springframework.stereotype.Component;

/**
 * 일반게시판(post_base) 상세 정보 설정 구현체
 */
@Component
public class PostBaseHandler implements PostDetailHandler {

    @Override
    public boolean supports(PostType type) {
        return switch (type) {
            case QNA, FREE, AIPLAY -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Post post, PostDTO dto) {
        if(dto.getBaseInfo() == null) return;
        PostBase postBase = new PostBase();
        postBase.setPost(post);
        postBase.setTags(dto.getBaseInfo().getTags());
        post.getPostBases().add(postBase);
    }
}