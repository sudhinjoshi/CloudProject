package com.nci.prj.repositories;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;

public interface S3Services {

    public ResponseEntity<Resource> downloadFile(String keyName);

    public void uploadFile(String keyName, File uploadFilePath);
}
