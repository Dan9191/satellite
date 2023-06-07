package com.example.satellite.utils;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Утилита для выкачивания файла.
 */
public class HttpFileUtils {

    /**
     * Оборачивает файл в поток.
     *
     * @param file     Файл.
     * @param fileName Имя файла.
     * @return Поток данных.
     */
    public static ResponseEntity<InputStreamResource> uploadFile(File file, String fileName) throws FileNotFoundException {
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String contentDisposition = String.format("attachment; filename=\"%s\"", fileName);
        return (ResponseEntity.ok().header("Content-Disposition", contentDisposition))
                .contentLength(file.length()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
    }
}