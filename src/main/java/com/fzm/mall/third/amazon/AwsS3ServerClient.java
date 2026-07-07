package com.fzm.mall.third.amazon;

import com.fzm.mall.constant.response.ResponseEnum;
import com.fzm.mall.constant.response.ValidateException;
import com.fzm.mall.entity.properties.FileProperties;
import com.fzm.mall.third.FileClient;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
public class AwsS3ServerClient extends FileClient {

    public AwsS3ServerClient(FileProperties properties) {
        super(properties.getAwsS3());
    }

    public void upload(String suffixName, byte[] fileBytes) {
        try (S3Client s3Client = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKeyId(), properties.accessKeySecret())))
                .build()) {
            s3Client.putObject(PutObjectRequest.builder().bucket(properties.bucket()).key(suffixName).build(), RequestBody.fromBytes(fileBytes));
        } catch (S3Exception e) {
            log.error("AwsS3服务异常，错误信息：{}", e.getMessage());
            throw new ValidateException(ResponseEnum.file_upload_failed);
        } catch (Exception e) {
            log.error("AwsS3服务异常，错误信息：", e);
            throw new ValidateException(ResponseEnum.file_upload_failed);
        }
    }

}
