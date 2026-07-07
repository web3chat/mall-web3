package com.fzm.mall.entity.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "local.file")
public class FileProperties {
    private Properties aliOss;
    private Properties awsS3;

    public record Properties(
            Boolean enable,
            String bucket,
            String region,
            String endpoint,
            String accessKeyId,
            String accessKeySecret
    ) {
    }
}
