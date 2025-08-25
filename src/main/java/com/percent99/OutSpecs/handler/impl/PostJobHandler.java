package com.percent99.OutSpecs.handler.impl;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.*;
import com.percent99.OutSpecs.handler.PostDetailHandler;
import org.springframework.stereotype.Component;

/**
 * 채용공고(posts_job) 상세 정보 설정 구현체
 */
@Component
public class PostJobHandler implements PostDetailHandler {

    @Override
    public boolean supports(PostType type) {
        return type == PostType.RECRUIT;
    }

    @Override
    public void handle(Post post, PostDTO dto) {

        if(dto.getJobInfo() == null){
            return;
        }
        PostJob job = post.getPostJob();
        if(job == null){
            job = new PostJob();
            job.setPost(post);
            post.setPostJob(job);
        }
        job.setCareer(dto.getJobInfo().getCareer());
        job.getTechniques().clear();
        for(String tech : dto.getJobInfo().getTechniques()){
            Techniques techniques = new Techniques();
            techniques.setPostJob(job);
            techniques.setTech(tech);
            job.getTechniques().add(techniques);
        }
    }
}