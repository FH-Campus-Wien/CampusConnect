package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.services.ChatService;
import at.ac.hcw.campusconnect.services.ProfileService;
import at.ac.hcw.campusconnect.services.SessionManager;
import at.ac.hcw.campusconnect.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

import java.io.IOException;

public class MainController {

    @FXML
    private Button discoverButton;
    @FXML
    private Button matchesButton;
    @FXML
    private Button chatsButton;
    @FXML
    private Button settingsButton;
    @FXML
    private StackPane contentArea;
    @FXML
    private StackPane sidebarAvatar;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label unreadBadge;

    private SessionManager sessionManager;
    private ProfileService profileService;
    private ChatService chatService;
    private Profile currentProfile;

    public void initialize() {
        sessionManager = SessionManager.getInstance();
        profileService = new ProfileService(sessionManager);
        chatService = new ChatService(sessionManager);

        // Load user profile and setup UI
        loadUserProfile();

        // Setup unread messages checker
        setupUnreadChecker();

        // Load discover view by default
        showDiscover();
    }

    private void loadUserProfile() {
        profileService.getProfile(sessionManager.getCurrentUser().getId())
                .thenAccept(profile -> {
                    Platform.runLater(() -> {
                        if (profile != null) {
                            currentProfile = profile;
                            userNameLabel.setText(profile.getFirstName() + " " + profile.getLastName());

                            // Load avatar
                            if (profile.getImageUrls() != null && !profile.getImageUrls().isEmpty()) {
                                try {
                                    Image image = new Image(profile.getImageUrls().get(0), true);
                                    ImageView imageView = new ImageView(image);
                                    imageView.setFitWidth(40);
                                    imageView.setFitHeight(40);
                                    imageView.setPreserveRatio(true);

                                    Circle clip = new Circle(20, 20, 20);
                                    imageView.setClip(clip);

                                    sidebarAvatar.getChildren().clear();
                                    sidebarAvatar.getChildren().add(imageView);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void setupUnreadChecker() {
        // Check for unread messages every 30 seconds
        Thread unreadChecker = new Thread(() -> {
            while (true) {
                chatService.getUnreadCount().thenAccept(count -> {
                    Platform.runLater(() -> {
                        if (count > 0) {
                            unreadBadge.setText(String.valueOf(count));
                            unreadBadge.setVisible(true);
                            unreadBadge.setManaged(true);
                        } else {
                            unreadBadge.setVisible(false);
                            unreadBadge.setManaged(false);
                        }
                    });
                });

                try {
                    Thread.sleep(30000); // 30 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        unreadChecker.setDaemon(true);
        unreadChecker.start();
    }

    @FXML
    private void showDiscover() {
        loadView("/at/ac/hcw/campusconnect/discover.fxml");
        updateActiveButton(discoverButton);
    }

    @FXML
    private void showMatches() {
        loadView("/at/ac/hcw/campusconnect/matches.fxml");
        updateActiveButton(matchesButton);
    }

    @FXML
    private void showChats() {
        loadView("/at/ac/hcw/campusconnect/chats.fxml");
        updateActiveButton(chatsButton);
    }

    @FXML
    private void showSettings() {
        loadView("/at/ac/hcw/campusconnect/settings.fxml");
        updateActiveButton(settingsButton);
    }

    private void loadView(String fxmlPath) {
        try {
            // Extract filename from path (e.g., "/at/ac/hcw/campusconnect/discover.fxml" -> "discover.fxml")
            String fileName = fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1);
            SceneNavigator.loadViewIntoContainer(contentArea, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button activeButton) {
        // Remove focused style from all buttons
        discoverButton.getStyleClass().remove("focused");
        matchesButton.getStyleClass().remove("focused");
        chatsButton.getStyleClass().remove("focused");
        settingsButton.getStyleClass().remove("focused");

        // Set focused on active button
        if (!activeButton.getStyleClass().contains("focused")) {
            activeButton.getStyleClass().add("focused");
        }

        // Request focus
        activeButton.requestFocus();
    }

    @FXML
    private void handleLogout() {
        sessionManager.signOut();

        try {
            SceneNavigator.switchScene(contentArea, "login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
