package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.repository.PostRepository;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class HomePostDTO {
  private long postId;
  private String title;
  private LocalDateTime createdAt;
  private PostType postType;
  private long likes;

  public static HomePostDTO toDTO(PostRepository.HomePostDTOProjection projection){
    HomePostDTO dto = new HomePostDTO();

    dto.setPostId(projection.getId());
    dto.setTitle(projection.getTitle());
    dto.setCreatedAt(projection.getCreatedAt());
    dto.setPostType(projection.getPostType());
    dto.setLikes(projection.getLikes());

    return dto;
  }
}
