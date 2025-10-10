package com.racoonsfinds.backend.config;


import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.access-key-id}")
    private String accessKey;

    @Value("${aws.secret-access-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    // @Bean
    // public AmazonS3 amazonS3() {
    //     BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
    //     return AmazonS3ClientBuilder.standard()
    //             .withRegion(region)
    //             .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
    //             .build();
    // }
    @Bean
    public S3Client getS3Client() {
        AwsBasicCredentials basicCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
            .region(Region.of(region))
            .endpointOverride(URI.create("https://s3." + region + ".amazonaws.com"))
            .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
            .build();
    }
}