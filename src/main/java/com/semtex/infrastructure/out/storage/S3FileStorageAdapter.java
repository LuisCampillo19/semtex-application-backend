package com.semtex.infrastructure.out.storage;

import com.semtex.domain.port.out.FileStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Year;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de almacenamiento sobre S3/MinIO.
 *
 * La clave del objeto es {@code <organizationId>/<año>/<uuid>-<archivo-saneado>}, lo que mantiene
 * los binarios particionados por tenant. El bucket se crea perezosamente en el primer uso para no
 * fallar el arranque si MinIO aún no está disponible.
 */
@Component
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3;
    private final String bucket;
    private volatile boolean bucketReady = false;

    public S3FileStorageAdapter(S3Client s3,
                                @Value("${semtex.storage.bucket:semtex-documents}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public String store(StoreFileCommand command) {
        ensureBucket();
        String key = buildKey(command.organizationId(), command.originalFilename());
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(command.contentType())
                        .build(),
                RequestBody.fromBytes(command.content()));
        return key;
    }

    @Override
    public Optional<byte[]> load(String storagePath) {
        try {
            ResponseBytes<GetObjectResponse> object = s3.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucket).key(storagePath).build());
            return Optional.of(object.asByteArray());
        } catch (NoSuchKeyException | NoSuchBucketException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String storagePath) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(storagePath).build());
    }

    private void ensureBucket() {
        if (bucketReady) return;
        synchronized (this) {
            if (bucketReady) return;
            try {
                s3.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            } catch (NoSuchBucketException e) {
                s3.createBucket(b -> b.bucket(bucket));
            }
            bucketReady = true;
        }
    }

    private String buildKey(UUID organizationId, String originalFilename) {
        String safeName = (originalFilename == null ? "archivo" : originalFilename)
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .toLowerCase(Locale.ROOT);
        return organizationId + "/" + Year.now().getValue() + "/" + UUID.randomUUID() + "-" + safeName;
    }
}
