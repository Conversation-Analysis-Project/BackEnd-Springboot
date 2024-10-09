package com.sometimes.code.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.sometimes.code.exception.ErrorCode;
import com.sometimes.code.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class S3ImageService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    // 폴더명을 매개변수로 받아서 이미지 업로드
    public String uploadImage(MultipartFile image, String folderName) {
        if (image == null || image.isEmpty() || image.getOriginalFilename() == null) {
            return null;  // 이미지가 없을 경우 null 반환
        }
        return this.uploadImageToS3(image, folderName);  // 폴더명에 맞게 업로드
    }

    private String uploadImageToS3(MultipartFile image, String folderName) {
        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        // 폴더명을 포함한 S3 파일명 설정
        String s3FileName = folderName + "/" + UUID.randomUUID().toString().substring(0, 10) + "_" + originalFilename;

        try (InputStream is = image.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(is);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/" + extension);
            metadata.setContentLength(bytes.length);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            // ACL 제거
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata);

            amazonS3.putObject(putObjectRequest);  // S3에 이미지 업로드

            return amazonS3.getUrl(bucketName, s3FileName).toString();
        } catch (IOException e) {
            throw new S3Exception(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }

    public void deleteImageFromS3(String imageUrl) {
        String key = getKeyFromImageUrl(imageUrl);
        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucketName, key));
        } catch (Exception e) {
            throw new S3Exception(ErrorCode.IO_EXCEPTION_ON_IMAGE_DELETE);
        }
    }

    private String getKeyFromImageUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            String key = URLDecoder.decode(url.getPath(), "UTF-8");
            return key.substring(1); // 맨 앞의 '/' 제거
        } catch (Exception e) {
            throw new S3Exception(ErrorCode.INVALID_URL);
        }
    }
}
