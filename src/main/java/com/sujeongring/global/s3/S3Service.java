package com.sujeongring.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.prefix}")
    private String prefix;
    
    public String getPresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    public String uploadProfileImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        String objectKey =
                prefix
                        + "/profile-images/"
                        + UUID.randomUUID()
                        + getExtension(file.getOriginalFilename());

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(file.getContentType())
                        .build();

        try {
            s3Client.putObject(
                    request,
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new IllegalStateException(
                    "프로필 이미지 업로드에 실패했습니다.",
                    e
            );
        }

        return objectKey;
    }

    private String getExtension(String fileName) {

        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(
                fileName.lastIndexOf(".")
        );
    }
}