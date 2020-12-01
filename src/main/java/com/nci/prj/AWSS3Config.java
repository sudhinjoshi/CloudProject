package com.nci.prj;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public AmazonS3 getAmazonS3Cient() {
        
        InstanceProfileCredentialsProvider credentials =
        InstanceProfileCredentialsProvider.createAsyncRefreshingProvider(true);
 
        return AmazonS3Client.builder()
             .withCredentials(credentials)
               .build();
 
        //12. // This is new: When you are done with the credentials provider, you must close it to release the background thread.
        /*
        try {
            credentials.close();
        } catch (Exception e) {
            System.out.println("Error in closing the credentials");
        }
        */
        /*
        final BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        // Get AmazonS3 client and return the s3Client object.
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
                .build();
        */        
                
                
                
    }
    
    
}
