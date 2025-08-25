package com.percent99.OutSpecs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 이미지 s3에 저장, 삭제를 위한 s3서비스
 */
@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public S3Service(@Value("${aws.access-key-id}") String accessKey,
                            @Value("${aws.secret-access-key}") String secretKey,
                            @Value("${aws.region}") String region,
                            @Value("${aws.s3.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.region = region;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    /**
     * 이미지 파일 AWS S3에 업로드
     * @param file
     * @return s3업로드 파일명
     * @throws IOException
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if(getFileType(file).contentEquals("")) {
            throw new IllegalArgumentException("허용되지 않은 이미지 파일 타입입니다.");
        }

        String fileName = generateFileName(file.getOriginalFilename());

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));

        return getFileUrl(fileName);
    }

    /**
     * 파일 다운로드 URL 생성 (미리 서명된 URL)
     */
    public String getFileUrl(String fileName) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        // 공개 URL (버킷이 public일 경우)
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, fileName);
    }

    /**
     * 파일 삭제
     * @param fileName
     */
    public void deleteFile(String fileName) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(request);
    }

    /**
     * 버킷의 모든 파일 목록 조회
     */
    public List<String> listFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        return response.contents().stream()
                .map(S3Object::key)
                .toList();

    }

    /**
     *  이미지 확장자 리턴 메서드
     * @param multipartFile
     * @return 이미지 확장자
     */
    private String getFileType(MultipartFile multipartFile) {
        String contentType = multipartFile.getContentType();
        if (contentType != null) {
            MediaType mediaType = MediaType.parseMediaType(contentType);
            switch (mediaType.toString()) {
                case MediaType.IMAGE_JPEG_VALUE -> { return ".jpeg"; }
                case MediaType.IMAGE_PNG_VALUE -> { return ".png"; }
            }
        }
        return "";
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateFileName(String originalFilename) {
        return "trade-images/" + UUID.randomUUID().toString() + "-" + originalFilename;
    }
}
