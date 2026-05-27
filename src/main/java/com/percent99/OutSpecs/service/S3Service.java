package com.percent99.OutSpecs.service;

import org.springframework.beans.factory.annotation.Qualifier;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 이미지 s3에 저장, 삭제를 위한 s3서비스
 */
@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName;
    private final String region;
    private final Executor executor;

    public S3Service(@Value("${aws.access-key-id}") String accessKey,
                            @Value("${aws.secret-access-key}") String secretKey,
                            @Value("${aws.region}") String region,
                            @Value("${aws.s3.bucket-name}") String bucketName,
                     @Qualifier("s3UploadExecutor") Executor executor) {
        this.bucketName = bucketName;
        this.region = region;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        this.executor = executor;
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

    public List<String> uploadSingleFile(MultipartFile file) throws IOException {
      ArrayList<String> result = new ArrayList<>();
      String key = generateFileName(file.getOriginalFilename());

      PutObjectRequest req = PutObjectRequest.builder()
              .bucket(bucketName)
              .key(key)
              .contentType(file.getContentType())
              .build();

      s3Client.putObject(req, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      result.add(getFileUrl(key));
      result.add(key);

      return result;
    }

    public Map<String, ArrayList<String>> uploadMultipleFiles(List<MultipartFile> files) throws IllegalStateException {
      Map<String, ArrayList<String>> result = new HashMap<>();

      result.put("urls", new ArrayList<>());
      result.put("keys", new ArrayList<>());

      List<CompletableFuture<List<String>>> future = files.stream()
              .filter(f -> !f.isEmpty())
              .map(file -> CompletableFuture.supplyAsync(() -> {
                try {
                  return this.uploadSingleFile(file);
                }catch (IOException e){
                  throw new IllegalStateException(e);
                }
              }, executor))
              .collect(Collectors.toList());

      List<List<String>> rl = future.stream()
              .map(CompletableFuture::join)
              .collect(Collectors.toList());

      for (List<String> r: rl){
        result.get("urls").add(r.get(0));
        result.get("keys").add(r.get(1));
      }

      return result;
    }

    public void deleteFiles(List<String> keys){
      List<ObjectIdentifier> delKeys = new ArrayList<>();

      for (String key: keys){
        delKeys.add(ObjectIdentifier.builder().key(key).build());
      }

      DeleteObjectsRequest delReq = DeleteObjectsRequest.builder()
              .bucket(bucketName)
              .delete(d -> d.objects(delKeys))
              .build();

      s3Client.deleteObjects(delReq);
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
