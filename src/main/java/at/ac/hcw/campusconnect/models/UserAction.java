package at.ac.hcw.campusconnect.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAction {
    @JsonProperty("id")
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("target_user_id")
    private String targetUserId;

    @JsonProperty("action")
    private String action; // "like" or "pass"

    @JsonProperty("created_at")
    private String createdAt;
}
