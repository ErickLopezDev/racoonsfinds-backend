package com.racoonsfinds.backend.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

  private final S3Client s3;
  @Value("${aws.s3.bucket}")
  private String bucket;

  @Value("${aws.region}")
  private String region;

  public S3Service(S3Client s3) {
    this.s3 = s3;
  }

  /**
   * Sube el archivo y devuelve la key (folder/filename.ext)
   */
  public String uploadFile(MultipartFile file, String folder) throws IOException {
    String original = file.getOriginalFilename();
    String filename = System.currentTimeMillis() + "-" + (original != null ? original.replace(" ", "_") : "file");
    String key = folder + "/" + filename;

    PutObjectRequest putReq = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(file.getContentType())
        .build();

    s3.putObject(putReq, RequestBody.fromBytes(file.getBytes()));
    return key;
  }

  /**
   * Construye una URL pública para un objecto si tu bucket permite acceso.
   * Si el bucket es privado deberías generar presigned URLs. Aquí construimos la
   * URL pública
   * usando el endpoint estándar.
   */
  public String getFileUrl(String key) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
  }
}
