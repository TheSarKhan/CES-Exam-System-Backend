package com.ces.exam.controller;

import com.ces.exam.exception.ValidationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Image upload (admin) + public serving for image-based questions/choices.
 * Files live on a mounted volume (app.upload-dir); they're served by opaque
 * UUID filename through a public endpoint so candidate <img> tags can load them
 * without an auth header.
 */
@RestController
public class ImageController {

    private static final long MAX_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Map<String, String> ALLOWED = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );
    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg", "webp", "gif");

    @Value("${app.upload-dir:/app/uploads}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    void init() throws IOException {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    /** Admin-only (secured by the default authenticated rule). Returns the public URL. */
    @PostMapping("/api/v1/images")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Fayl boşdur");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ValidationException("Şəkil 5 MB-dan böyük ola bilməz");
        }
        String ext = ALLOWED.get(file.getContentType());
        if (ext == null) {
            throw new ValidationException("Yalnız PNG, JPG, WEBP və ya GIF şəkillər qəbul olunur");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new ValidationException("Yanlış fayl adı");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);
        }
        return ResponseEntity.ok(Map.of("filename", filename, "url", "/api/v1/public/images/" + filename));
    }

    /** Public so candidate browsers can load <img> without an Authorization header. */
    @GetMapping("/api/v1/public/images/{filename}")
    public ResponseEntity<Resource> serve(@PathVariable String filename) throws IOException {
        // Hard reject anything that isn't a bare safe filename — no path traversal.
        if (!filename.matches("[A-Za-z0-9]+\\.[A-Za-z0-9]+")) {
            return ResponseEntity.notFound().build();
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            return ResponseEntity.notFound().build();
        }
        Path file = root.resolve(filename).normalize();
        if (!file.startsWith(root) || !Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        MediaType type = "png".equals(ext) ? MediaType.IMAGE_PNG
                : ("gif".equals(ext) ? MediaType.IMAGE_GIF
                : ("webp".equals(ext) ? MediaType.parseMediaType("image/webp") : MediaType.IMAGE_JPEG));
        Resource body = new InputStreamResource(Files.newInputStream(file));
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable")
                .contentLength(Files.size(file))
                .body(body);
    }
}
