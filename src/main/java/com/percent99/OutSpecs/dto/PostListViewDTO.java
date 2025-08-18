package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.Image;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostListViewDTO {
    private Long id;
    private String title;
    private String content;        // 템플릿의 post.content에 대응(요약을 넣어도 됨)
    private User user;             // 템플릿의 post.user.username에 대응
    private PostType type;
    private LocalDateTime createdAt;
    private Long viewCount;
    private long likeCount;
    private long commentCount;
    private long bookmarkCount;
    private List<Image> images;
}