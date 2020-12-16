package com.nci.prj.services;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.nci.prj.repositories.S3Services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Service - S3ServiceImpl
 * <p>
 * This service is implementing AWS S3 integration
 *
 * @author Sudhindra Joshi
 */
@Service
public class S3ServicesImpl implements S3Services {

    @Autowired
    private AmazonS3 s3client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    /**
     * Method perform Object download operation from AWS S3
     *
     * @param keyName - Object Name to be downloaded
     * @return specification file
     */
    @Override
    public ResponseEntity<Resource> downloadFile(String keyName) {

        System.out.println("Downloading an object.");
        S3Object s3object = s3client.getObject(new GetObjectRequest(bucketName, keyName));
        System.out.println("Content-Type: " + s3object.getObjectMetadata().getContentType());

        S3ObjectInputStream finalObject = s3object.getObjectContent();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .cacheControl(CacheControl.noCache())
                .header("Content-Disposition", "attachment; filename=" + "testing.pdf")
                .body(new InputStreamResource(finalObject));

    }

    /**
     * Method perform Object upload operation in AWS S3
     *
     * @param keyName - Object Name to be uploaded
     * @param file    - Specification File to be uploaded
     */
    @Override
    public void uploadFile(String keyName, final File file) {
        try {

            s3client.putObject(new PutObjectRequest(bucketName, keyName, file));
            System.out.println("===================== Upload File - Done! =====================");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException from PUT requests, rejected reasons:");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException: ");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
