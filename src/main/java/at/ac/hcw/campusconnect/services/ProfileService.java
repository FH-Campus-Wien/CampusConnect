package at.ac.hcw.campusconnect.services;

import at.ac.hcw.campusconnect.config.SupabaseConfig;
import at.ac.hcw.campusconnect.models.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing user profiles in Supabase.
 * Handles creating and updating profile data using the Supabase REST API.
 */
public class ProfileService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;

    public ProfileService(SessionManager sessionManager) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.sessionManager = sessionManager;
    }

    /**
     * Creates a new profile in Supabase.
     *
     * @param profile The profile data to create
     * @return CompletableFuture with the created profile
     * @throws Exception if the request fails
     */
    public CompletableFuture<Profile> createProfile(Profile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get the current user's access token
                String accessToken = sessionManager.getAccessToken();
                if (accessToken == null) {
                    throw new RuntimeException("User not authenticated");
                }

                // Set the user ID from the session
                String userId = sessionManager.getCurrentUser() != null ?
                        sessionManager.getCurrentUser().getId() : null;
                if (userId == null) {
                    throw new RuntimeException("User ID not found");
                }
                profile.setUserId(userId);

                // Convert profile to JSON
                String jsonBody = objectMapper.writeValueAsString(profile);

                // Build the HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles"))
                        .header("Content-Type", "application/json")
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Prefer", "return=representation")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                // Send the request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Check response status
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    // Parse response as array and get first element
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

    /**
     * Updates an existing profile in Supabase.
     *
     * @param profile The profile data to update
     * @return CompletableFuture with the updated profile
     * @throws Exception if the request fails
     */
    public CompletableFuture<Profile> updateProfile(Profile profile) {
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

                // Convert profile to JSON
                String jsonBody = objectMapper.writeValueAsString(profile);

                // Build the HTTP request with user_id filter
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?user_id=eq." + userId))
                        .header("Content-Type", "application/json")
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Prefer", "return=representation")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                // Send the request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Check response status
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    // Parse response as array and get first element
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

    /**
     * Retrieves the profile for the current user.
     *
     * @return CompletableFuture with the user's profile, or null if not found
     * @throws Exception if the request fails
     */
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

                // Send the request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Check response status
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    // Parse response as array
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
