package at.ac.hcw.campusconnect.services;

import at.ac.hcw.campusconnect.config.SupabaseConfig;
import at.ac.hcw.campusconnect.models.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ProfileService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;

    public ProfileService(SessionManager sessionManager) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.sessionManager = sessionManager;
    }

    public CompletableFuture<Profile> createProfile(Profile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String accessToken = sessionManager.getAccessToken();
                if (accessToken == null) {
                    throw new RuntimeException("User not authenticated");
                }

                String userId = sessionManager.getCurrentUser() != null ?
                        sessionManager.getCurrentUser().getId() : null;
                if (userId == null) {
                    throw new RuntimeException("User ID not found");
                }
                profile.setUserId(userId);

                String jsonBody = objectMapper.writeValueAsString(profile);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles"))
                        .header("Content-Type", "application/json")
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Prefer", "return=representation")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    Profile[] profiles = objectMapper.readValue(response.body(), Profile[].class);
                    if (profiles.length > 0) {
                        return profiles[0];
                    }
                    throw new RuntimeException("No profile returned from server");
                } else {
                    throw new RuntimeException("Failed to create profile: " + response.statusCode() + " - " + response.body());
                }

            } catch (Exception e) {
                throw new RuntimeException("Error creating profile: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Profile> updateProfile(Profile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String accessToken = sessionManager.getAccessToken();
                if (accessToken == null) {
                    throw new RuntimeException("User not authenticated");
                }

                String userId = sessionManager.getCurrentUser() != null ?
                        sessionManager.getCurrentUser().getId() : null;
                if (userId == null) {
                    throw new RuntimeException("User ID not found");
                }

                String jsonBody = objectMapper.writeValueAsString(profile);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?user_id=eq." + userId))
                        .header("Content-Type", "application/json")
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Prefer", "return=representation")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    Profile[] profiles = objectMapper.readValue(response.body(), Profile[].class);
                    if (profiles.length > 0) {
                        return profiles[0];
                    }
                    throw new RuntimeException("No profile returned from server");
                } else {
                    throw new RuntimeException("Failed to update profile: " + response.statusCode() + " - " + response.body());
                }

            } catch (Exception e) {
                throw new RuntimeException("Error updating profile: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Profile> getProfile() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get the current user's access token
                String accessToken = sessionManager.getAccessToken();
                if (accessToken == null) {
                    throw new RuntimeException("User not authenticated");
                }

                String userId = sessionManager.getCurrentUser() != null ?
                        sessionManager.getCurrentUser().getId() : null;
                if (userId == null) {
                    throw new RuntimeException("User ID not found");
                }

                // Build the HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?user_id=eq." + userId))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    Profile[] profiles = objectMapper.readValue(response.body(), Profile[].class);
                    if (profiles.length > 0) {
                        return profiles[0];
                    }
                    return null; // Profile not found
                } else {
                    throw new RuntimeException("Failed to get profile: " + response.statusCode() + " - " + response.body());
                }

            } catch (Exception e) {
                throw new RuntimeException("Error getting profile: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Profile> getProfile(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String accessToken = sessionManager.getAccessToken();
                if (accessToken == null) {
                    throw new RuntimeException("User not authenticated");
                }

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?user_id=eq." + userId))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    Profile[] profiles = objectMapper.readValue(response.body(), Profile[].class);
                    if (profiles.length > 0) {
                        return profiles[0];
                    }
                    return null; // Profile not found
                } else {
                    throw new RuntimeException("Failed to get profile: " + response.statusCode() + " - " + response.body());
                }

            } catch (Exception e) {
                throw new RuntimeException("Error getting profile: " + e.getMessage(), e);
            }
        });
    }
}
