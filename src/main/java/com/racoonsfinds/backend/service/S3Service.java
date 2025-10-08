package com.racoonsfinds.backend.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.racoonsfinds.backend.shared.exception.FileStorageException;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Upload file to S3 and return the public URL
     */
    public String uploadFile(MultipartFile file) {
        try {
            // Generate unique filename
            String fileName = "products/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
            
            // Get file input stream
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            
            // Upload to S3
            amazonS3.putObject(bucketName, fileName, file.getInputStream(), metadata);
            
            // Make object public readable
            amazonS3.setObjectAcl(bucketName, fileName, CannedAccessControlList.PublicRead);
            
            // Return public URL
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Delete file from S3
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractFileNameFromUrl(fileUrl);
            amazonS3.deleteObject(bucketName, fileName);
        } catch (Exception e) {
            throw new FileStorageException("Could not delete file from S3", e);
        }
    }

    /**
     * Extract filename from S3 URL
     */
    private String extractFileNameFromUrl(String fileUrl) {
        String bucketUrl = "https://" + bucketName + ".s3.amazonaws.com/";
        if (fileUrl.startsWith(bucketUrl)) {
            return fileUrl.substring(bucketUrl.length());
        }
        throw new IllegalArgumentException("Invalid S3 URL: " + fileUrl);
    }
}