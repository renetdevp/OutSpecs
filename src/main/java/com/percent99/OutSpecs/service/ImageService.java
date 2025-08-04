package com.percent99.OutSpecs.service;

import com.percent99.OutSpecs.entity.Image;
import com.percent99.OutSpecs.entity.Post;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ImageService {

    Image uploadImage(Post post, MultipartFile file) throws IOException;
    List<Image> getAllImages();
    Optional<Image> getImage(Long id);
    void deleteImage(Long id) throws Exception;
    String extractKeyFromUrl(String url);
}
