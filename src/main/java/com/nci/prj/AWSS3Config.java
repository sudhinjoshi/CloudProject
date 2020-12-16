package com.nci.prj;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration - AWSS3Config
 * <p>
 * This Configuration is a placeholder for S3 interface
 *
 * @author Sudhindra Joshi
 */
@Configuration
public class AWSS3Config {

    // Access key id will be read from the application.properties file during the application intialization.
    @Value("${aws.access_key_id}")
    private String accessKeyId;
    // Secret access key will be read from the application.properties file during the application intialization.
    @Value("${aws.secret_access_key}")
    private String secretAccessKey;
    // Region will be read from the application.properties file  during the application intialization.
    @Value("${aws.s3.region}")
    private String region;

    /**
     * Bean to get custom S3Client
     *
     * @return AmazonS3 client
     */
    @Bean
    public AmazonS3 getAmazonS3Cient() {

        InstanceProfileCredentialsProvider credentials =
                InstanceProfileCredentialsProvider.createAsyncRefreshingProvider(true);

        return AmazonS3Client.builder()
                .withCredentials(credentials)
                .withRegion(region)
                .build();
    }
}
