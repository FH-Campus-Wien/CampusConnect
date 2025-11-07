package at.ac.hcw.campusconnect.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String id;
    private String email;
    private String phone;
    private String role;

    @JsonProperty("aud")
    private String audience;

    @JsonProperty("email_confirmed_at")
    private String emailConfirmedAt;

    @JsonProperty("phone_confirmed_at")
    private String phoneConfirmedAt;

    @JsonProperty("confirmation_sent_at")
    private String confirmationSentAt;

    @JsonProperty("confirmed_at")
    private String confirmedAt;

    @JsonProperty("last_sign_in_at")
    private String lastSignInAt;

    @JsonProperty("app_metadata")
    private Map<String, Object> appMetadata;

    @JsonProperty("user_metadata")
    private Map<String, Object> userMetadata;

    private List<Identity> identities;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("is_anonymous")
    private boolean isAnonymous;

    @Data
    @NoArgsConstructor
    public static class Identity {
        @JsonProperty("identity_id")
        private String identityId;

        private String id;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("identity_data")
        private Map<String, Object> identityData;

        private String provider;

        @JsonProperty("last_sign_in_at")
        private String lastSignInAt;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        private String email;
    }
}