package at.ac.hcw.campusconnect.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("expires_at")
    private long expiresAt;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private User user;

    public boolean isExpired() {
        return System.currentTimeMillis() / 1000 >= expiresAt;
    }

    public long getTimeUntilExpiry() {
        return expiresAt - (System.currentTimeMillis() / 1000);
    }
}