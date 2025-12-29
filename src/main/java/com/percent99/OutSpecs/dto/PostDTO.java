package com.percent99.OutSpecs.dto;

import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.PostTags;
import com.percent99.OutSpecs.entity.PostType;
import com.percent99.OutSpecs.entity.Techniques;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 게시글(posts) 생성 및 수정 시 전달되는 데이터 전송 객체(DTO)
 * <ul>
 *     <li>기본 게시글 정보 : 작성자 id, 유형(type), 제목(title), 내용(content) </li>
 *     <lI>1:1 매핑 엔티티 정보를 각각의 하위 DTO로 포함 (teamInfo, jobInfo, baseInfo, hangoutInfo)</lI>
 * </ul>
 */

@Getter
@Setter
public class PostDTO {

    private Long userId;
    private Long postId;
    private PostType type;
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    private PostTeamInformationDTO teamInfo;
    private PostJobDTO jobInfo;
    private PostTagsDTO tagsInfo;
    private PostHangoutDTO hangoutInfo;
    private PostQnADTO qnaInfo;

    public static PostDTO toDTO(Post post){
      PostDTO dto = new PostDTO();

      dto.setUserId(post.getUser().getId());
      dto.setPostId(post.getId());
      dto.setType(post.getType());
      dto.setTitle(post.getTitle());
      dto.setContent(post.getContent());

      addTagsInfo(dto, post);
      addHangoutInfo(dto, post);
      addJobInfo(dto, post);
      addTeamInfo(dto, post);
      addQnAInfo(dto, post);

      return dto;
    }

    private static void addTagsInfo(PostDTO dto, Post post) {
      if (post.getPostTags() == null || post.getPostTags().isEmpty()){
        return;
      }

      PostTagsDTO tagsDTO = new PostTagsDTO();
      String tags = post.getPostTags().stream()
              .map(PostTags::getTags) // tag 객체 자체가 아닌, 태그 이름을 가져옵니다.
              .collect(Collectors.joining(","));
      tagsDTO.setTags(tags);
      dto.setTagsInfo(tagsDTO);
    }

    private static void addHangoutInfo(PostDTO dto, Post post) {
      if (post.getPostHangout() == null){
        return;
      }

      PostHangoutDTO hangoutDTO = new PostHangoutDTO();
      hangoutDTO.setPlaceName(post.getPostHangout().getPlaceName());
      dto.setHangoutInfo(hangoutDTO);
    }

    private static void addJobInfo(PostDTO dto, Post post) {
      if (post.getPostJob() == null){
        return;
      }

      PostJobDTO jobDTO = new PostJobDTO();
      jobDTO.setCareer(post.getPostJob().getCareer());
      List<String> techNames = post.getPostJob().getTechniques().stream()
              .map(Techniques::getTech)
              .toList();
      jobDTO.setTechniques(techNames);
      dto.setJobInfo(jobDTO);
    }

    private static void addTeamInfo(PostDTO dto, Post post) {
      if (post.getTeamInfo() == null){
        return;
      }

      PostTeamInformationDTO teamInfoDTO = new PostTeamInformationDTO();
      teamInfoDTO.setCapacity(post.getTeamInfo().getCapacity());
      teamInfoDTO.setStatus(post.getTeamInfo().getStatus());
      dto.setTeamInfo(teamInfoDTO);
    }

    private static void addQnAInfo(PostDTO dto, Post post) {
      if (post.getPostQnA() == null){
        return;
      }

      PostQnADTO qnaDTO = new PostQnADTO();
      qnaDTO.setAnswerComplete(post.getPostQnA().getAnswerComplete());
      dto.setQnaInfo(qnaDTO);
    }
}