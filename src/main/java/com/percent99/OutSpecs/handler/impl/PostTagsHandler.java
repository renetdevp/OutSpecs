package com.percent99.OutSpecs.handler.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostTags;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import org.springframework.stereotype.Component;

/**
 * QnA, Free 태그 상세 정보 설정 구현체
 */
@Component
public class PostTagsHandler implements PostDetailHandler {

    @Override
    public boolean supports(PostType type) {
        return switch (type) {
            case QNA, FREE -> true;
            default -> false;
        };
    }

    @Override
    public void handle(Post post, PostDTO dto) {
        if(dto.getTagsInfo() == null) return;
        PostTags postTags = new PostTags();
        postTags.setPost(post);
        postTags.setTags(dto.getTagsInfo().getTags());
        post.getPostTags().add(postTags);
    }
}