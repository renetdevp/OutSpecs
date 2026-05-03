package com.percent99.OutSpecs.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PostResponseDTO {
    private Long likesCount;
    private Long commentsCount;
    private Long answersCount;
    private boolean isLiked;
    private boolean isBookmarked;
    private boolean isReported;
    private boolean isParticipation;
    private Long teamCount;
}
