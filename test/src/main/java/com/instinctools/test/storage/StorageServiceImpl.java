package com.instinctools.test.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageServiceImpl implements IStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger("storageLogger");
    private final Path rootLocation;

    @Autowired
    public StorageServiceImpl(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) throws StorageException{
        try {
            if (file.isEmpty()) {
                StorageException storageException = new StorageException("Failed to store empty file " + file.getOriginalFilename());
            	LOGGER.warn(storageException.getMessage());
				throw storageException;
            }
            Files.copy(file.getInputStream(), this.rootLocation.resolve(file.getOriginalFilename()));
        } catch (IOException e) {
            StorageException storageException = new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        	LOGGER.warn(storageException.getMessage());
			throw storageException;
        }
    }

    @Override
    public void delete(Path path) {
            FileSystemUtils.deleteRecursively(path.toFile());
    }

}
