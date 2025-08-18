package com.percent99.OutSpecs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostResponseDTO {
    private int likesCount;
    private int commentsCount;
    private int answersCount;
    private boolean isLiked;
    private boolean isBookmarked;
    private int teamCount;
}
