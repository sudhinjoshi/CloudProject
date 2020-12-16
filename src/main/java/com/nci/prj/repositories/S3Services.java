package com.nci.prj.repositories;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * Interface S3Services for AWS S3 integration
 * <p>
 *
 * @author Sudhindra Joshi
 */
public interface S3Services {

    public ResponseEntity<Resource> downloadFile(String keyName);

    public void uploadFile(String keyName, File uploadFilePath);
}
