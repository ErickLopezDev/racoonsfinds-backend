package com.racoonsfinds.backend.platform.storage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("dev")
public class InMemoryS3Service implements S3Service {

    private final Map<String, StoredFile> files = new ConcurrentHashMap<>();

    @Override
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String original = file.getOriginalFilename();
        String filename = System.currentTimeMillis() + "-" + (original != null ? original.replace(" ", "_") : "file");
        String key = folder + "/" + filename;

        files.put(key, new StoredFile(file.getBytes(), file.getContentType()));
        return key;
    }

    @Override
    public String getFileUrl(String key) {
        return "memory://s3/" + key;
    }

    public boolean exists(String key) {
        return files.containsKey(key);
    }

    private record StoredFile(byte[] content, String contentType) {
    }
}
