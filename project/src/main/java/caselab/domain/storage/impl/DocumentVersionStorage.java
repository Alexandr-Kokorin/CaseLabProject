package caselab.domain.storage.impl;

import caselab.domain.storage.FileStorage;
import caselab.domain.storage.exception.DocumentStorageException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import static org.apache.commons.io.FilenameUtils.getExtension;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentVersionStorage implements FileStorage {

    private final MinioClient minioClient;

    @Value("${minio.buckets}")
    private String bucket;

    @Override
    public String put(MultipartFile file) {

        if(file.isEmpty() || file.getOriginalFilename() == null){
            log.error("Document upload failed. Document must have name");
            throw new DocumentStorageException("Document must have name");
        }

        String fileName = generateFileName(file);

        try (InputStream inputStream = file.getInputStream()) {
            upload(inputStream, fileName);
        } catch (IOException e) {
            throw new DocumentStorageException(e.getMessage(), e.getCause());
        }

        log.debug("Document was successfully uploaded with name: " + fileName);
        return fileName;
    }

    private void upload(InputStream inputStream, String fileName){
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .stream(inputStream, inputStream.available(), -1)
                    .bucket(bucket)
                    .object(fileName)
                    .build());
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException |
                 IOException e) {
            throw new DocumentStorageException(e.getMessage(), e.getCause());
        }
    }

    private String generateFileName(MultipartFile file){
        String extension = getExtension(file.getOriginalFilename());
        return UUID.randomUUID() + "." + extension;
    }

    @Override
    public InputStream get(String fileName) {
        try {
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | XmlParserException |
                 ServerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(String fileName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(fileName)
                    .build());

            return true;
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new DocumentStorageException(e.getMessage(), e.getCause());
        }
    }
}
