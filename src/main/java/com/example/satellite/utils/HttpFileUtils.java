package com.example.satellite.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lombok.NoArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

@NoArgsConstructor
public class HttpFileUtils {

    public static ResponseEntity<InputStreamResource> uploadFile(File file, String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        String contentDisposition = String.format("attachment; filename=\"%s\"", fileName);
        return ((BodyBuilder)ResponseEntity.ok().header("Content-Disposition", new String[]{contentDisposition})).contentLength(file.length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }
}