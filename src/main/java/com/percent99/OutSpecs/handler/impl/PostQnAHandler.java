package com.percent99.OutSpecs.handler.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostQnA;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.handler.PostDetailHandler;

public class PostQnAHandler implements PostDetailHandler {

    @Override
    public boolean supports(PostType type) {
        return type == PostType.QNA;
    }

    @Override
    public void handle(Post post, PostDTO dto) {
        if(dto.getQnaInfo() == null) return;
        PostQnA postQnA = new PostQnA();
        postQnA.setPost(post);
        postQnA.setAnswerComplete(dto.getQnaInfo().isAnswerComplete());
        post.setPostQnA(postQnA);
    }
}
