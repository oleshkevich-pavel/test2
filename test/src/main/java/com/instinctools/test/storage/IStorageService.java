package com.instinctools.test.storage;

import java.nio.file.Path;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {

    void store(MultipartFile file);

    void delete(Path path);

}
