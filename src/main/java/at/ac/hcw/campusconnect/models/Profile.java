package at.ac.hcw.campusconnect.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class Profile {
    @JsonProperty("id")
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("birthdate")
    private String birthdate; // Format: YYYY-MM-DD

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("pronouns")
    private String pronouns;

    @JsonProperty("degree_type")
    private String degreeType;

    @JsonProperty("semester")
    private Integer semester;

    @JsonProperty("study_program")
    private String studyProgram;

    @JsonProperty("looking_for")
    private String lookingFor;

    @JsonProperty("interested_in")
    private String interestedIn;

    @JsonProperty("bio")
    private String bio;

    @JsonProperty("interests")
    private Set<String> interests;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
