package at.ac.hcw.campusconnect.services;

import at.ac.hcw.campusconnect.config.SupabaseConfig;
import at.ac.hcw.campusconnect.models.Match;
import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.models.UserAction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MatchService {
    private final SessionManager sessionManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public MatchService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get profiles to discover based on current user's preferences
     */
    public CompletableFuture<List<Profile>> getDiscoverProfiles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentUserId = sessionManager.getCurrentUser().getId();

                // First, get current user's profile to filter by preferences
                Profile currentProfile = getCurrentUserProfile();
                if (currentProfile == null) {
                    return new ArrayList<>();
                }

                // Get all profiles except current user
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?select=*&user_id=neq." + currentUserId))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<Profile> allProfiles = objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Profile>>() {
                            }
                    );

                    // Get user's existing actions (likes and passes)
                    Set<String> actionedUserIds = getActionedUserIds(currentUserId);

                    // Filter profiles
                    List<Profile> filteredProfiles = allProfiles.stream()
                            .filter(p -> !actionedUserIds.contains(p.getUserId())) // Not already actioned
                            .filter(p -> isCompatible(currentProfile, p)) // Compatible preferences
                            .collect(Collectors.toList());

                    // Sort by interest match (more shared interests first)
                    filteredProfiles.sort((p1, p2) -> {
                        int match1 = countSharedInterests(currentProfile, p1);
                        int match2 = countSharedInterests(currentProfile, p2);
                        return Integer.compare(match2, match1); // Descending order
                    });

                    return filteredProfiles;
                }
                return new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * Record a user action (like or pass)
     */
    public CompletableFuture<Boolean> recordAction(String targetUserId, String action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentUserId = sessionManager.getCurrentUser().getId();

                Map<String, String> actionData = new HashMap<>();
                actionData.put("user_id", currentUserId);
                actionData.put("target_user_id", targetUserId);
                actionData.put("action", action);

                String jsonBody = objectMapper.writeValueAsString(actionData);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/user_actions"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 201;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Get all matches for current user
     */
    public CompletableFuture<List<Match>> getMatches() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentUserId = sessionManager.getCurrentUser().getId();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/matches?or=(user1_id.eq." + currentUserId + ",user2_id.eq." + currentUserId + ")&order=matched_at.desc"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Match>>() {
                            }
                    );
                }
                return new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        });
    }

    /**
     * Get profile for a matched user
     */
    public CompletableFuture<Profile> getMatchedProfile(String matchedUserId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?user_id=eq." + matchedUserId))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<Profile> profiles = objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Profile>>() {
                            }
                    );
                    return profiles.isEmpty() ? null : profiles.get(0);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    // Helper methods

    private Profile getCurrentUserProfile() {
        try {
            String currentUserId = sessionManager.getCurrentUser().getId();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SupabaseConfig.getRestUrl() + "/profiles?user_id=eq." + currentUserId))
                    .header("apikey", SupabaseConfig.getSupabaseKey())
                    .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Profile> profiles = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<Profile>>() {
                        }
                );
                return profiles.isEmpty() ? null : profiles.get(0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Set<String> getActionedUserIds(String currentUserId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SupabaseConfig.getRestUrl() + "/user_actions?user_id=eq." + currentUserId + "&select=target_user_id"))
                    .header("apikey", SupabaseConfig.getSupabaseKey())
                    .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<UserAction> actions = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<UserAction>>() {
                        }
                );
                return actions.stream()
                        .map(UserAction::getTargetUserId)
                        .collect(Collectors.toSet());
            }
            return new HashSet<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    private boolean isCompatible(Profile currentProfile, Profile otherProfile) {
        String currentLookingFor = currentProfile.getLookingFor(); // What current user wants
        String currentInterestedIn = currentProfile.getInterestedIn(); // Gender preference
        String otherLookingFor = otherProfile.getLookingFor();
        String otherInterestedIn = otherProfile.getInterestedIn();
        String currentGender = currentProfile.getGender();
        String otherGender = otherProfile.getGender();

        // Check if looking_for is compatible (Friends, Dates, or Both)
        boolean lookingForMatch = isLookingForCompatible(currentLookingFor, otherLookingFor);

        // Check gender preferences
        boolean genderMatch = isGenderCompatible(currentInterestedIn, otherGender) &&
                isGenderCompatible(otherInterestedIn, currentGender);

        return lookingForMatch && genderMatch;
    }

    private boolean isLookingForCompatible(String lookingFor1, String lookingFor2) {
        // "Both" is compatible with everything
        if ("Both".equals(lookingFor1) || "Both".equals(lookingFor2)) {
            return true;
        }
        // Otherwise they must match
        return lookingFor1.equals(lookingFor2);
    }

    private boolean isGenderCompatible(String interestedIn, String gender) {
        return "Everyone".equals(interestedIn) || interestedIn.equals(gender);
    }

    private int countSharedInterests(Profile p1, Profile p2) {
        if (p1.getInterests() == null || p2.getInterests() == null) {
            return 0;
        }
        Set<String> interests1 = new HashSet<>(p1.getInterests());
        Set<String> interests2 = new HashSet<>(p2.getInterests());
        interests1.retainAll(interests2);
        return interests1.size();
    }
}
