package at.ac.hcw.campusconnect.services;

import at.ac.hcw.campusconnect.config.SupabaseConfig;
import at.ac.hcw.campusconnect.models.Message;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ChatService {
    private final SessionManager sessionManager;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ChatService(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get all messages for a match
     */
    public CompletableFuture<List<Message>> getMessages(String matchId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/messages?match_id=eq." + matchId + "&order=created_at.asc"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Message>>() {}
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
     * Send a message
     */
    public CompletableFuture<Message> sendMessage(String matchId, String receiverId, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentUserId = sessionManager.getCurrentUser().getId();
                
                Map<String, String> messageData = new HashMap<>();
                messageData.put("match_id", matchId);
                messageData.put("sender_id", currentUserId);
                messageData.put("receiver_id", receiverId);
                messageData.put("content", content);

                String jsonBody = objectMapper.writeValueAsString(messageData);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/messages"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=representation")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 201) {
                    List<Message> messages = objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Message>>() {}
                    );
                    return messages.isEmpty() ? null : messages.get(0);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Mark messages as read
     */
    public CompletableFuture<Boolean> markMessagesAsRead(String matchId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentUserId = sessionManager.getCurrentUser().getId();
                
                Map<String, Boolean> updateData = new HashMap<>();
                updateData.put("is_read", true);

                String jsonBody = objectMapper.writeValueAsString(updateData);
                
                // Update all messages in this match where current user is receiver and is_read is false
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/messages?match_id=eq." + matchId + "&receiver_id=eq." + currentUserId + "&is_read=eq.false"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.statusCode() == 204 || response.statusCode() == 200;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    /**
     * Get last message for a match
     */
    public CompletableFuture<Message> getLastMessage(String matchId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/messages?match_id=eq." + matchId + "&order=created_at.desc&limit=1"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    List<Message> messages = objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Message>>() {}
                    );
                    return messages.isEmpty() ? null : messages.get(0);
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    /**
     * Get unread message count for current user
     */
    public CompletableFuture<Integer> getUnreadCount() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String currentUserId = sessionManager.getCurrentUser().getId();
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SupabaseConfig.getRestUrl() + "/messages?receiver_id=eq." + currentUserId + "&is_read=eq.false&select=id"))
                        .header("apikey", SupabaseConfig.getSupabaseKey())
                        .header("Authorization", "Bearer " + sessionManager.getAccessToken())
                        .header("Range-Unit", "items")
                        .header("Prefer", "count=exact")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 || response.statusCode() == 206) {
                    // Get count from Content-Range header
                    String contentRange = response.headers().firstValue("Content-Range").orElse("");
                    if (contentRange.contains("/")) {
                        String[] parts = contentRange.split("/");
                        return Integer.parseInt(parts[1]);
                    }
                    
                    // Fallback: count items in response
                    List<Message> messages = objectMapper.readValue(
                            response.body(),
                            new TypeReference<List<Message>>() {}
                    );
                    return messages.size();
                }
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
    }
}
