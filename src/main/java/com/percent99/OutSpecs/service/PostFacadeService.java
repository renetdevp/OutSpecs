package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.dto.PostDTO;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.entity.User;
import com.percent99.OutSpecs.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostFacadeService {
  private final PostService postService;
  private final S3Service s3Service;
  private final UserRepository userRepository;

  public Post writePost(PostDTO dto, List<MultipartFile> files) {
    User user = userRepository.getReferenceById(dto.getUserId());
    List<String> uploadedUrls = new ArrayList<>();
    List<String> s3Keys = new ArrayList<>();

    try {
      if (files != null && !files.isEmpty()) {
        Map<String, ArrayList<String>> result = s3Service.uploadMultipleFiles(files);

        uploadedUrls = result.get("urls");
        s3Keys = result.get("keys");
      }

      return postService.createdPostDB(user, dto, uploadedUrls, s3Keys);
    }catch (IllegalStateException e){
      s3Service.deleteFiles(s3Keys);

      throw e;
    }
  }
}
