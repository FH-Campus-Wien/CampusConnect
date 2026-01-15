package at.ac.hcw.campusconnect.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Match {
    @JsonProperty("id")
    private String id;

    @JsonProperty("user1_id")
    private String user1Id;

    @JsonProperty("user2_id")
    private String user2Id;

    @JsonProperty("matched_at")
    private String matchedAt;
    
    // Helper fields (not from database)
    private Profile matchedProfile; // The other user's profile
    private Message lastMessage; // Last message in this match
}
