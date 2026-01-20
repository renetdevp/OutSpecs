package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.repository.PostRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HomePostDTO {
  private long postId;
  private String title;
  private LocalDateTime createdAt;
  private PostType postType;
  private long likes;

  public static HomePostDTO toDTO(PostRepository.HomePostDTOProjection projection){
    return new HomePostDTO(
            projection.getId(),
            projection.getTitle(),
            projection.getCreatedAt(),
            projection.getPostType(),
            projection.getLikes()
    );
  }
}
