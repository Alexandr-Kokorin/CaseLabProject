package caselab.configuration;

import caselab.domain.storage.exception.InitStorageException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class StorageConfig {

    @Value("${minio.buckets}")
    private List<String> buckets;
    @Value("${minio.url}")
    private String url;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretkey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        try {
            return MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build();
        } catch (Exception exc) {
            log.error("Creating MinIO client failed");
            throw new InitStorageException(exc.getMessage(), exc.getCause());
        }
    }

    @Bean
    public boolean bucketsInit() {
        for (String bucket : buckets) {
            try {
                bucketInit(bucket);
            } catch (InitStorageException exc) {
                return false;
            }
        }
        return true;
    }

    private void bucketInit(String bucketName) {
        boolean bucketExist = bucketExists(bucketName);
        if (!bucketExist) {
            crateBucket(bucketName);
        } else {
            log.debug(bucketName + " bucket already exists");
        }
    }

    private boolean bucketExists(String bucketName) {
        try {
            return minioClient().bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                 | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException
                 | XmlParserException e) {
            log.error("An error occurred while checking the bucket " + bucketName);
            throw new InitStorageException(e.getMessage(), e.getCause());
        }
    }

    private void crateBucket(String bucketName) {
        try {
            minioClient().makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
                 | InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException
                 | XmlParserException e) {
            log.error("An error occurred while initializing the bucket " + bucketName);
            throw new InitStorageException(e.getMessage(), e.getCause());
        }

        log.debug(bucketName + "bucket successfully created");
    }

}

