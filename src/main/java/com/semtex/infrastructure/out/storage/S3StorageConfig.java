package com.semtex.infrastructure.out.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/**
 * Cliente S3 apuntando a MinIO en dev (endpoint local, path-style) o a Supabase Storage / S3 en prod.
 */
@Configuration
public class S3StorageConfig {

    @Bean
    public S3Client s3Client(
            @Value("${semtex.storage.endpoint:http://localhost:9000}") String endpoint,
            @Value("${semtex.storage.region:us-east-1}") String region,
            @Value("${semtex.storage.access-key:minioadmin}") String accessKey,
            @Value("${semtex.storage.secret-key:minioadmin}") String secretKey,
            @Value("${semtex.storage.path-style-access:true}") boolean pathStyle) {

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(pathStyle)
                        .build())
                .build();
    }
}
