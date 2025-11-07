package at.ac.hcw.campusconnect.services;

import at.ac.hcw.campusconnect.config.SupabaseConfig;
import at.ac.hcw.campusconnect.models.AuthResponse;
import at.ac.hcw.campusconnect.models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.prefs.Preferences;

public class AuthService {
    private static final String PREFS_NODE = "io.knotzer.campusconnect.auth";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Preferences prefs;

    // In-memory session data
    @Getter
    private volatile String accessToken;

    private volatile User currentUser;

    private volatile long expiresAt;

    public AuthService() {
        this.prefs = Preferences.userRoot().node(PREFS_NODE);
    }

    public boolean sendOTP(String email) {
        try {
            Map<String, String> requestBody = Map.of("email", email);
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SupabaseConfig.getAuthUrl() + "/otp"))
                    .header("apikey", SupabaseConfig.getSupabaseKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyOTP(String email, String token) {
        try {
            Map<String, String> requestBody = Map.of(
                    "email", email,
                    "token", token,
                    "type", "email"
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SupabaseConfig.getAuthUrl() + "/verify"))
                    .header("apikey", SupabaseConfig.getSupabaseKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AuthResponse authResponse = objectMapper.readValue(
                        response.body(), AuthResponse.class);
                setSession(authResponse);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreSession() {
        String refreshToken = prefs.get(KEY_REFRESH_TOKEN, null);
        if (refreshToken == null) {
            return false;
        }

        return refreshToken(refreshToken);
    }

    public boolean refreshToken() {
        String refreshToken = prefs.get(KEY_REFRESH_TOKEN, null);
        if (refreshToken == null) {
            return false;
        }
        return refreshToken(refreshToken);
    }


    private boolean refreshToken(String refreshToken) {
        try {
            Map<String, String> requestBody = Map.of("refresh_token", refreshToken);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SupabaseConfig.getAuthUrl() +
                            "/token?grant_type=refresh_token"))
                    .header("apikey", SupabaseConfig.getSupabaseKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                AuthResponse authResponse = objectMapper.readValue(
                        response.body(), AuthResponse.class);
                setSession(authResponse);
                return true;
            } else {
                // Refresh token invalid, clear it
                clearPersistedData();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setSession(AuthResponse authResponse) {
        // Store in memory
        this.accessToken = authResponse.getAccessToken();
        this.currentUser = authResponse.getUser();
        this.expiresAt = authResponse.getExpiresAt();

        // Persist refresh token and user info for auto-login
        if (authResponse.getRefreshToken() != null) {
            prefs.put(KEY_REFRESH_TOKEN, authResponse.getRefreshToken());
        }

        if (authResponse.getUser() != null) {
            prefs.put(KEY_USER_ID, authResponse.getUser().getId());
            prefs.put(KEY_USER_EMAIL, authResponse.getUser().getEmail());
        }
    }

    public String getValidAccessToken() {
        if (accessToken != null && !isTokenExpired()) {
            return accessToken;
        }

        // Token expired or missing, try to refresh
        if (refreshToken()) {
            return accessToken;
        }

        return null;
    }

    private boolean isTokenExpired() {
        long currentTime = System.currentTimeMillis() / 1000;
        // Add 5 minute buffer to refresh proactively
        return expiresAt - currentTime < 300;
    }

    public boolean needsTokenRefresh() {
        long currentTime = System.currentTimeMillis() / 1000;
        long fiveMinutes = 5 * 60;
        return (expiresAt - currentTime) < fiveMinutes;
    }

    public boolean isAuthenticated() {
        return getValidAccessToken() != null;
    }

    public boolean hasStoredSession() {
        return prefs.get(KEY_REFRESH_TOKEN, null) != null;
    }

    public User getCurrentUser() {
        if (currentUser != null) {
            return currentUser;
        }

        // Try to reconstruct from stored data
        String userId = prefs.get(KEY_USER_ID, null);
        String email = prefs.get(KEY_USER_EMAIL, null);

        if (userId != null && email != null) {
            User user = new User();
            user.setId(userId);
            user.setEmail(email);
            this.currentUser = user;
            return user;
        }

        return null;
    }

    /**
     * Check if user has completed profile setup
     * TODO: Implement actual profile check with API call
     */
    public boolean hasProfile() {
        // For now, return false to force profile setup
        return false;
    }

    public void signOut() {
        // Try to invalidate token on server
        if (accessToken != null) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getAuthUrl() + "/logout"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + accessToken)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

                client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                // Ignore errors during logout
            }
        }

        // Clear in-memory data
        this.accessToken = null;
        this.currentUser = null;
        this.expiresAt = 0;

        // Clear persisted data
        clearPersistedData();
    }

    private void clearPersistedData() {
        prefs.remove(KEY_REFRESH_TOKEN);
        prefs.remove(KEY_USER_ID);
        prefs.remove(KEY_USER_EMAIL);
    }

    public HttpRequest.Builder getAuthenticatedRequestBuilder(String url) {
        String token = getValidAccessToken();
        if (token == null) {
            throw new IllegalStateException("No valid authentication token available");
        }

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.getSupabaseKey())
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json");
    }
}
