package com.feature.neighbourHood_backend.service;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class SupabaseStorageService {

    private S3Client s3Client;

    @Value("${supabase.endpoint}")
    private String supabaseEndpoint;

    @Value("${supabase.bucket}")
    private String bucketName;

    @Value("${supabase.bucket.profile-images:}")
    private String profileImagesBucket;

    @Value("${supabase.bucket.post-images:}")
    private String postImagesBucket;

    @Value("${supabase.bucket.documents:}")
    private String documentsBucket;

    @Value("${supabase.bucket.chat-videos:}")
    private String chatVideosBucket;

    @Value("${supabase.bucket.chat-images:}")
    private String chatImagesBucket;

    @Value("${supabase.bucket.chat-audio:}")
    private String chatAudioBucket;

    @Autowired
    public SupabaseStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, String path) {
        try {
            String selectedBucket = resolveBucket(file, path);
            // to identify the object uniquely.
            String key = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

            // Upload the file to S3 bucket
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(selectedBucket)
                    .key(path + "/" + key)
                    .build(), RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return generatePublicUrl(selectedBucket, path + "/" + key);

        } catch (IOException exception) {
            return exception.getMessage();
        }
    }

    private String resolveBucket(MultipartFile file, String path) {
        String normalizedPath = path == null ? "" : path.toLowerCase(Locale.ROOT);
        String contentType = file == null || file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        // Priority 1: explicit route/path hints.
        if (normalizedPath.contains("profile")) {
            return pickConfiguredBucket(profileImagesBucket);
        }
        if (normalizedPath.contains("document") || normalizedPath.contains("doc")) {
            return pickConfiguredBucket(documentsBucket);
        }
        if (normalizedPath.contains("video")) {
            return pickConfiguredBucket(chatVideosBucket);
        }
        if (normalizedPath.contains("audio") || normalizedPath.contains("voice")) {
            return pickConfiguredBucket(chatAudioBucket);
        }
        if (normalizedPath.contains("chat") && normalizedPath.contains("image")) {
            return pickConfiguredBucket(chatImagesBucket);
        }
        if (normalizedPath.contains("post") || normalizedPath.contains("request")) {
            return pickConfiguredBucket(postImagesBucket);
        }

        // Priority 2: file content type.
        if (contentType.startsWith("video/")) {
            return pickConfiguredBucket(chatVideosBucket);
        }
        if (contentType.startsWith("audio/")) {
            return pickConfiguredBucket(chatAudioBucket);
        }
        if (contentType.startsWith("image/")) {
            if (normalizedPath.contains("chat")) {
                return pickConfiguredBucket(chatImagesBucket);
            }
            return pickConfiguredBucket(postImagesBucket);
        }
        if (contentType.equals("application/pdf")
                || contentType.equals("text/plain")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return pickConfiguredBucket(documentsBucket);
        }

        // Fallback to default/single-bucket mode.
        return pickConfiguredBucket(bucketName);
    }

    private String pickConfiguredBucket(String preferredBucket) {
        if (preferredBucket != null && !preferredBucket.isBlank()) {
            return preferredBucket;
        }
        return bucketName;
    }

    private String generatePublicUrl(String selectedBucket, String filePath) {
        return supabaseEndpoint + "/storage/v1/object/public/" + selectedBucket + "/" + filePath;
    }
}