package com.percent99.OutSpecs.service.Impl;

import com.percent99.OutSpecs.entity.Image;
import com.percent99.OutSpecs.entity.Post;
import com.percent99.OutSpecs.repository.ImageRepository;
import com.percent99.OutSpecs.service.ImageService;
import com.percent99.OutSpecs.service.S3Service;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 이미지 등록, 수정, 삭제, 조회 서비스
 */
@Service
public class ImageServiceImpl implements ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;

    public ImageServiceImpl(S3Service s3Service, ImageRepository imageRepository) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
    }

    /**
     * 이미지 AWS s3에 업로드 후 DB 저장
     * @param post
     * @param file
     * @return 저장된 image
     * @throws IOException
     */
    @Override
    public Image uploadImage(Post post, MultipartFile file) throws IOException {
        String s3Url = s3Service.uploadFile(file);
        String s3Key = extractKeyFromUrl(s3Url);

        Image image = new Image(post, s3Url, s3Key);
        return imageRepository.save(image);
    }

    /**
     * 모든 이미지 조회
     * @return 모든 이미지
     */

    @Override
    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    /**
     * 이미지 id로 이미지 조회
     * @param id
     * @return 이미지
     */
    @Override
    public Optional<Image> getImage(Long id) {
        return imageRepository.findById(id);
    }

    /**
     * s3에서 이미지 삭제 후 이미지 DB 삭제
     * @param id
     * @throws Exception
     */
    @Override
    public void deleteImage(Long id) throws Exception {
        Image image = imageRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("이미지를 찾을 수 없습니다."));

        s3Service.deleteFile(image.getS3Key());
        imageRepository.deleteById(id);
    }

    /**
     * 이미지 url로 부터 s3key 추출
     * @param url
     * @return s3 key
     */
    @Override
    public String extractKeyFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
