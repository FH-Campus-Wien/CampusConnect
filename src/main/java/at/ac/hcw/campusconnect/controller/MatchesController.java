package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.components.ErrorBox;
import at.ac.hcw.campusconnect.models.Match;
import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.services.MatchService;
import at.ac.hcw.campusconnect.services.SessionManager;
import at.ac.hcw.campusconnect.util.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;

public class MatchesController {

    @FXML
    private ErrorBox errorBox;
    @FXML
    private GridPane matchesGrid;
    @FXML
    private VBox emptyState;
    @FXML
    private ProgressIndicator loadingIndicator;

    private SessionManager sessionManager;
    private MatchService matchService;

    public void initialize() {
        sessionManager = SessionManager.getInstance();
        matchService = new MatchService(sessionManager);

        loadMatches();
    }

    private void loadMatches() {
        loadingIndicator.setVisible(true);
        matchesGrid.setVisible(false);
        emptyState.setVisible(false);

        matchService.getMatches()
                .thenAccept(matches -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        
                        if (matches == null || matches.isEmpty()) {
                            emptyState.setVisible(true);
                        } else {
                            displayMatches(matches);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        errorBox.showError("Failed to load matches. Please try again.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void displayMatches(List<Match> matches) {
        matchesGrid.getChildren().clear();
        matchesGrid.setVisible(true);

        String currentUserId = sessionManager.getCurrentUser().getId();
        
        for (int i = 0; i < matches.size(); i++) {
            Match match = matches.get(i);
            
            String matchedUserId = match.getUser1Id().equals(currentUserId) ? 
                    match.getUser2Id() : match.getUser1Id();

            int row = i / 3;
            int col = i % 3;

            VBox card = createMatchCard(null);
            matchesGrid.add(card, col, row);

            matchService.getMatchedProfile(matchedUserId)
                    .thenAccept(profile -> {
                        Platform.runLater(() -> {
                            if (profile != null) {
                                VBox filledCard = createMatchCard(profile);
                                matchesGrid.getChildren().remove(card);
                                matchesGrid.add(filledCard, col, row);
                            }
                        });
                    });
        }
    }

    private VBox createMatchCard(Profile profile) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("match-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(180);
        card.setPrefHeight(220);

        if (profile != null) {
            // Profile image
            StackPane avatarContainer = new StackPane();
            avatarContainer.setPrefSize(120, 120);
            avatarContainer.getStyleClass().add("match-avatar");

            if (profile.getImageUrls() != null && !profile.getImageUrls().isEmpty()) {
                try {
                    Image image = new Image(profile.getImageUrls().get(0), 120, 120, true, true, true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(120);
                    imageView.setFitHeight(120);
                    imageView.setPreserveRatio(false);
                    avatarContainer.getChildren().add(imageView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Label nameLabel = new Label(profile.getFirstName() + " " + profile.getLastName());
            nameLabel.getStyleClass().add("match-name");
            nameLabel.setWrapText(true);
            nameLabel.setMaxWidth(160);

            Label infoLabel = new Label(profile.getStudyProgram());
            infoLabel.getStyleClass().add("match-info");
            infoLabel.setWrapText(true);
            infoLabel.setMaxWidth(160);

            card.getChildren().addAll(avatarContainer, nameLabel, infoLabel);

            card.setOnMouseClicked(event -> openChat(profile));
        } else {
            ProgressIndicator loader = new ProgressIndicator();
            loader.setPrefSize(50, 50);
            card.getChildren().add(loader);
        }

        return card;
    }

    private void openChat(Profile profile) {
        try {
            Parent root = errorBox.getScene().getRoot();
            if (root instanceof BorderPane) {
                BorderPane borderPane = (BorderPane) root;
                
                StackPane contentArea = (StackPane) borderPane.getCenter();
                ChatsController controller = SceneNavigator.loadViewIntoContainerWithController(contentArea, "chats.fxml");
                
                controller.selectChatByProfile(profile);
                
                updateNavigationButtons(borderPane);
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorBox.showError("Failed to open chat.");
        }
    }
    
    private void updateNavigationButtons(BorderPane borderPane) {
        try {
            VBox sidebar = (VBox) borderPane.getLeft();
            Object userData = borderPane.getUserData();

            if (userData instanceof MainController) {
                MainController mainController = (MainController) userData;
                mainController.activateChatsButton();
            } else {
                // Fallback: manually find and focus the chats button
                findAndFocusChatsButton(sidebar);
            }
        } catch (Exception e) {
            // Ignore navigation button update errors
        }
    }
}
