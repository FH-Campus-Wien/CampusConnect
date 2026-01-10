package at.ac.hcw.campusconnect.services;


import at.ac.hcw.campusconnect.config.SupabaseConfig;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing image uploads to Supabase Storage.
 * Handles uploading up to 4 profile images per user.
 */
public class ImageStorageService {
    private static final String BUCKET_NAME = "profile-images";
    private static final int MAX_IMAGES = 4;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final HttpClient httpClient;
    private final SessionManager sessionManager;

    public ImageStorageService(SessionManager sessionManager) {
        this.httpClient = HttpClient.newHttpClient();
        this.sessionManager = sessionManager;
    }

    /**
     * Uploads multiple images to Supabase Storage.
     * Maximum of 4 images allowed.
     *
     * @param imageFiles List of image files to upload
     * @return CompletableFuture with list of public URLs for the uploaded images
     * @throws IllegalArgumentException if more than 4 images or invalid files
     */
    public CompletableFuture<List<String>> uploadImages(List<File> imageFiles) {
        // Validate number of images
        if (imageFiles.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed");
        }

        return CompletableFuture.supplyAsync(() -> {
            List<String> imageUrls = new ArrayList<>();

            for (File imageFile : imageFiles) {
                try {
                    String url = uploadSingleImage(imageFile);
                    imageUrls.add(url);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload image: " + imageFile.getName(), e);
                }
            }

            return imageUrls;
        });
    }

    /**
     * Uploads a single image file to Supabase Storage.
     *
     * @param imageFile The image file to upload
     * @return The public URL of the uploaded image
     * @throws Exception if upload fails
     */
    private String uploadSingleImage(File imageFile) throws Exception {
        // Validate file
        validateImageFile(imageFile);

        // Get access token
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("User not authenticated");
        }

        // Get user ID for organizing files
        String userId = sessionManager.getCurrentUser() != null ?
                sessionManager.getCurrentUser().getId() : null;
        if (userId == null) {
            throw new RuntimeException("User ID not found");
        }

        // Generate unique filename with user folder structure
        String fileExtension = getFileExtension(imageFile.getName());
        String fileName = userId + "/" + UUID.randomUUID().toString() + fileExtension;

        // Read file bytes
        byte[] fileBytes = Files.readAllBytes(imageFile.toPath());

        // Detect content type
        String contentType = Files.probeContentType(imageFile.toPath());
        if (contentType == null) {
            contentType = "image/jpeg"; // Default fallback
        }

        // Build upload URL
        String uploadUrl = SupabaseConfig.getStorageUrl() + "/object/" + BUCKET_NAME + "/" + fileName;

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uploadUrl))
                .header("apikey", SupabaseConfig.getSupabaseKey())
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileBytes))
                .build();

        // Send the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Check response status
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            // Return the public URL
            return getPublicUrl(fileName);
        } else {
            throw new RuntimeException("Failed to upload image: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Validates an image file before upload.
     * Checks file exists, size, and type.
     *
     * @param imageFile The file to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateImageFile(File imageFile) throws IOException {
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("File does not exist: " + imageFile.getName());
        }

        if (imageFile.length() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large: " + imageFile.getName() +
                    " (max " + (MAX_FILE_SIZE / 1024 / 1024) + "MB)");
        }

        // Check file extension
        String fileName = imageFile.getName().toLowerCase();
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") &&
                !fileName.endsWith(".png") && !fileName.endsWith(".gif") &&
                !fileName.endsWith(".webp")) {
            throw new IllegalArgumentException("Invalid file type. Supported: JPG, PNG, GIF, WEBP");
        }
    }

    /**
     * Gets the file extension from a filename.
     *
     * @param fileName The filename
     * @return The file extension including the dot (e.g., ".jpg")
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(lastDotIndex);
        }
        return ".jpg"; // Default extension
    }

    /**
     * Constructs the public URL for an uploaded image.
     *
     * @param fileName The filename/path in storage
     * @return The public URL
     */
    private String getPublicUrl(String fileName) {
        return SupabaseConfig.getStorageUrl() + "/object/public/" + BUCKET_NAME + "/" + fileName;
    }

    /**
     * Deletes an image from Supabase Storage.
     *
     * @param imageUrl The public URL of the image to delete
     * @return CompletableFuture indicating success
     */
    public CompletableFuture<Boolean> deleteImage(String imageUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Extract filename from URL
                String fileName = extractFileNameFromUrl(imageUrl);
                if (fileName == null) {
                    throw new RuntimeException("Invalid image URL");
                }

                // Get access token
                String accessToken = sessionManager.getAccessToken();
                if (accessToken == null) {
                    throw new RuntimeException("User not authenticated");
                }

                // Build delete URL
                String deleteUrl = SupabaseConfig.getStorageUrl() + "/object/" + BUCKET_NAME + "/" + fileName;

                // Build the HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(deleteUrl))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .DELETE()
                        .build();

                // Send the request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Check response status
                return response.statusCode() >= 200 && response.statusCode() < 300;

            } catch (Exception e) {
                throw new RuntimeException("Error deleting image: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Extracts the filename/path from a Supabase Storage public URL.
     *
     * @param imageUrl The public URL
     * @return The filename/path or null if invalid
     */
    private String extractFileNameFromUrl(String imageUrl) {
        String prefix = "/object/public/" + BUCKET_NAME + "/";
        int index = imageUrl.indexOf(prefix);
        if (index >= 0) {
            return imageUrl.substring(index + prefix.length());
        }
        return null;
    }
}

