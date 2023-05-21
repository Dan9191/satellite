package com.example.satellite.repository;

import com.example.satellite.entity.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UploadedFilesRepository extends JpaRepository<UploadedFile, Integer> {
    List<UploadedFile> findByName(String name);
}
