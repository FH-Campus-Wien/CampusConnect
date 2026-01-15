package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.components.ErrorBox;
import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.services.MatchService;
import at.ac.hcw.campusconnect.services.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DiscoverController {

    @FXML
    private ErrorBox errorBox;
    @FXML
    private StackPane cardContainer;
    @FXML
    private VBox emptyState;
    @FXML
    private VBox profileCard;
    @FXML
    private StackPane imageSection;
    @FXML
    private ImageView profileImage;
    @FXML
    private HBox imageIndicators;
    @FXML
    private Label profileName;
    @FXML
    private Label profileInfo;
    @FXML
    private Label profileBio;
    @FXML
    private FlowPane interestsPane;
    @FXML
    private Label lookingForLabel;
    @FXML
    private Label interestedInLabel;
    @FXML
    private HBox actionButtons;
    @FXML
    private Button passButton;
    @FXML
    private Button likeButton;
    @FXML
    private ProgressIndicator loadingIndicator;

    private SessionManager sessionManager;
    private MatchService matchService;
    private List<Profile> profiles;
    private int currentProfileIndex = 0;
    private int currentImageIndex = 0;

    public void initialize() {
        sessionManager = SessionManager.getInstance();
        matchService = new MatchService(sessionManager);

        loadProfiles();
        
        // Setup image navigation
        setupImageNavigation();
    }

    private void loadProfiles() {
        loadingIndicator.setVisible(true);
        profileCard.setVisible(false);
        actionButtons.setVisible(false);
        emptyState.setVisible(false);

        matchService.getDiscoverProfiles()
                .thenAccept(loadedProfiles -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        
                        if (loadedProfiles == null || loadedProfiles.isEmpty()) {
                            showEmptyState();
                        } else {
                            profiles = new ArrayList<>(loadedProfiles);
                            currentProfileIndex = 0;
                            showCurrentProfile();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        errorBox.showError("Failed to load profiles. Please try again.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void showCurrentProfile() {
        if (profiles == null || currentProfileIndex >= profiles.size()) {
            showEmptyState();
            return;
        }

        Profile profile = profiles.get(currentProfileIndex);
        currentImageIndex = 0;

        // Show card and buttons
        profileCard.setVisible(true);
        actionButtons.setVisible(true);
        emptyState.setVisible(false);

        // Set profile data
        int age = calculateAge(profile.getBirthdate());
        profileName.setText(profile.getFirstName() + ", " + age);
        profileInfo.setText(profile.getStudyProgram() + " • Semester " + profile.getSemester());
        profileBio.setText(profile.getBio());
        lookingForLabel.setText(profile.getLookingFor());
        interestedInLabel.setText(profile.getInterestedIn());

        // Load images
        if (profile.getImageUrls() != null && !profile.getImageUrls().isEmpty()) {
            displayImage(profile.getImageUrls().get(0));
            setupImageIndicators(profile.getImageUrls().size());
        } else {
            profileImage.setImage(null);
        }

        // Load interests
        interestsPane.getChildren().clear();
        if (profile.getInterests() != null) {
            for (String interest : profile.getInterests()) {
                Label tag = new Label(interest);
                tag.getStyleClass().add("interest-tag-selected");
                interestsPane.getChildren().add(tag);
            }
        }
    }

    private void displayImage(String imageUrl) {
        try {
            Image image = new Image(imageUrl, true);
            profileImage.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupImageIndicators(int count) {
        imageIndicators.getChildren().clear();
        
        // Hide indicators if only one image
        if (count <= 1) {
            imageIndicators.setVisible(false);
            imageIndicators.setManaged(false);
            return;
        }
        
        imageIndicators.setVisible(true);
        imageIndicators.setManaged(true);
        
        for (int i = 0; i < count; i++) {
            StackPane indicator = new StackPane();
            indicator.setPrefWidth(30);
            indicator.setMaxHeight(3);
            indicator.setMaxWidth(30);
            indicator.setStyle(i == 0 ? 
                    "-fx-background-color: white; -fx-background-radius: 2px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);" :
                    "-fx-background-color: rgba(255,255,255,0.4); -fx-background-radius: 2px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
            imageIndicators.getChildren().add(indicator);
        }
    }

    private void setupImageNavigation() {
        imageSection.setOnMouseClicked(event -> {
            if (profiles == null || currentProfileIndex >= profiles.size()) return;
            
            Profile profile = profiles.get(currentProfileIndex);
            if (profile.getImageUrls() == null || profile.getImageUrls().isEmpty()) return;

            double clickX = event.getX();
            double width = imageSection.getWidth();

            if (clickX < width / 2) {
                // Left side clicked - previous image
                currentImageIndex--;
                if (currentImageIndex < 0) {
                    currentImageIndex = profile.getImageUrls().size() - 1;
                }
            } else {
                // Right side clicked - next image
                currentImageIndex++;
                if (currentImageIndex >= profile.getImageUrls().size()) {
                    currentImageIndex = 0;
                }
            }

            displayImage(profile.getImageUrls().get(currentImageIndex));
            updateImageIndicators();
        });
    }

    private void updateImageIndicators() {
        for (int i = 0; i < imageIndicators.getChildren().size(); i++) {
            StackPane indicator = (StackPane) imageIndicators.getChildren().get(i);
            indicator.setStyle(i == currentImageIndex ?
                    "-fx-background-color: white; -fx-background-radius: 2px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);" :
                    "-fx-background-color: rgba(255,255,255,0.4); -fx-background-radius: 2px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0, 0, 1);");
        }
    }

    @FXML
    private void handleLike() {
        if (profiles == null || currentProfileIndex >= profiles.size()) return;

        Profile profile = profiles.get(currentProfileIndex);
        recordActionAndMoveNext("like", profile.getUserId());
    }

    @FXML
    private void handlePass() {
        if (profiles == null || currentProfileIndex >= profiles.size()) return;

        Profile profile = profiles.get(currentProfileIndex);
        recordActionAndMoveNext("pass", profile.getUserId());
    }

    private void recordActionAndMoveNext(String action, String targetUserId) {
        // Disable buttons during action
        likeButton.setDisable(true);
        passButton.setDisable(true);

        matchService.recordAction(targetUserId, action)
                .thenAccept(success -> {
                    Platform.runLater(() -> {
                        likeButton.setDisable(false);
                        passButton.setDisable(false);

                        if (success) {
                            // Move to next profile
                            currentProfileIndex++;
                            showCurrentProfile();
                        } else {
                            errorBox.showError("Failed to record action. Please try again.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        likeButton.setDisable(false);
                        passButton.setDisable(false);
                        errorBox.showError("An error occurred. Please try again.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void showEmptyState() {
        profileCard.setVisible(false);
        actionButtons.setVisible(false);
        emptyState.setVisible(true);
    }

    private int calculateAge(String birthdate) {
        try {
            LocalDate birth = LocalDate.parse(birthdate, DateTimeFormatter.ISO_DATE);
            return Period.between(birth, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }
}
