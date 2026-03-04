package com.swpts.enpracticebe.constant;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@EqualsAndHashCode
@Configuration
@ConfigurationProperties(prefix = "backblaze.s3")
public class S3Properties {
    private String endpoint;
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String workerSharedSecret;
}
