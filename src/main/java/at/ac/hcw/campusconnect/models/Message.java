package at.ac.hcw.campusconnect.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {
    @JsonProperty("id")
    private String id;

    @JsonProperty("match_id")
    private String matchId;

    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("receiver_id")
    private String receiverId;

    @JsonProperty("content")
    private String content;

    @JsonProperty("is_read")
    private Boolean isRead;

    @JsonProperty("created_at")
    private String createdAt;
}
