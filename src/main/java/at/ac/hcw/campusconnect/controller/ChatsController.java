package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.components.ErrorBox;
import at.ac.hcw.campusconnect.models.Match;
import at.ac.hcw.campusconnect.models.Message;
import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.services.ChatService;
import at.ac.hcw.campusconnect.services.MatchService;
import at.ac.hcw.campusconnect.services.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatsController {

    @FXML
    private ErrorBox errorBox;
    @FXML
    private VBox chatList;
    @FXML
    private VBox chatListEmptyState;
    @FXML
    private VBox chatArea;
    @FXML
    private StackPane chatAvatar;
    @FXML
    private Label chatUserName;
    @FXML
    private Label chatUserInfo;
    @FXML
    private ScrollPane messagesScrollPane;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageInput;
    @FXML
    private Button sendButton;
    @FXML
    private VBox emptyChatState;

    private SessionManager sessionManager;
    private MatchService matchService;
    private ChatService chatService;

    private List<Match> matches;
    private Match selectedMatch;
    private Profile selectedProfile;
    private Map<String, Profile> profileCache = new HashMap<>();

    private Thread messageRefreshThread;

    public void initialize() {
        sessionManager = SessionManager.getInstance();
        matchService = new MatchService(sessionManager);
        chatService = new ChatService(sessionManager);

        loadMatches();
    }

    private void loadMatches() {
        matchService.getMatches()
                .thenAccept(loadedMatches -> {
                    Platform.runLater(() -> {
                        if (loadedMatches == null || loadedMatches.isEmpty()) {
                            chatListEmptyState.setVisible(true);
                            chatList.setVisible(false);
                        } else {
                            matches = loadedMatches;
                            displayChatList();
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        errorBox.showError("Failed to load chats. Please try again.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void displayChatList() {
        chatList.getChildren().clear();
        chatList.setVisible(true);
        chatListEmptyState.setVisible(false);

        String currentUserId = sessionManager.getCurrentUser().getId();

        for (Match match : matches) {
            String matchedUserId = match.getUser1Id().equals(currentUserId) ?
                    match.getUser2Id() : match.getUser1Id();

            // Create chat item
            HBox chatItem = createChatItemPlaceholder();
            chatList.getChildren().add(chatItem);

            // Load profile
            matchService.getMatchedProfile(matchedUserId)
                    .thenAccept(profile -> {
                        if (profile != null) {
                            profileCache.put(matchedUserId, profile);
                            match.setMatchedProfile(profile);

                            // Load last message
                            chatService.getLastMessage(match.getId())
                                    .thenAccept(lastMessage -> {
                                        Platform.runLater(() -> {
                                            match.setLastMessage(lastMessage);
                                            HBox filledItem = createChatItem(match, profile, lastMessage);
                                            int index = chatList.getChildren().indexOf(chatItem);
                                            if (index >= 0) {
                                                chatList.getChildren().set(index, filledItem);
                                            }
                                        });
                                    });
                        }
                    });
        }
    }

    private HBox createChatItemPlaceholder() {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("chat-item");
        item.setPadding(new Insets(15, 20, 15, 20));

        StackPane avatar = new StackPane();
        avatar.setPrefSize(50, 50);
        avatar.getStyleClass().add("profile-avatar-small");

        VBox textContainer = new VBox(5);
        Label nameLabel = new Label("Loading...");
        nameLabel.getStyleClass().add("chat-name");
        textContainer.getChildren().add(nameLabel);

        item.getChildren().addAll(avatar, textContainer);
        return item;
    }

    private HBox createChatItem(Match match, Profile profile, Message lastMessage) {
        HBox item = new HBox(15);
        item.setAlignment(Pos.CENTER_LEFT);
        item.getStyleClass().add("chat-item");
        item.setPadding(new Insets(15, 20, 15, 20));
        item.setCursor(javafx.scene.Cursor.HAND);

        // Avatar
        StackPane avatarContainer = new StackPane();
        avatarContainer.setPrefSize(50, 50);
        avatarContainer.getStyleClass().add("profile-avatar-small");

        if (profile.getImageUrls() != null && !profile.getImageUrls().isEmpty()) {
            try {
                Image image = new Image(profile.getImageUrls().get(0), 50, 50, true, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(false);

                // Create circular clip
                Rectangle clip = new Rectangle(50, 50);
                clip.setArcWidth(50);
                clip.setArcHeight(50);
                imageView.setClip(clip);

                avatarContainer.getChildren().add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Text content
        VBox textContainer = new VBox(5);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label nameLabel = new Label(profile.getFirstName() + " " + profile.getLastName());
        nameLabel.getStyleClass().add("chat-name");

        if (lastMessage != null) {
            Label messageLabel = new Label(lastMessage.getContent());
            messageLabel.getStyleClass().add("chat-last-message");
            messageLabel.setMaxWidth(200);
            textContainer.getChildren().addAll(nameLabel, messageLabel);
        } else {
            Label messageLabel = new Label("Start chatting!");
            messageLabel.getStyleClass().add("chat-last-message");
            textContainer.getChildren().addAll(nameLabel, messageLabel);
        }

        // Time
        VBox timeContainer = new VBox();
        timeContainer.setAlignment(Pos.TOP_RIGHT);

        if (lastMessage != null) {
            Label timeLabel = new Label(formatTime(lastMessage.getCreatedAt()));
            timeLabel.getStyleClass().add("chat-time");
            timeContainer.getChildren().add(timeLabel);
        }

        item.getChildren().addAll(avatarContainer, textContainer, timeContainer);

        // Click handler
        item.setOnMouseClicked(event -> selectChat(match, profile));

        return item;
    }

    private void selectChat(Match match, Profile profile) {
        selectedMatch = match;
        selectedProfile = profile;

        // Update header
        chatUserName.setText(profile.getFirstName() + " " + profile.getLastName());
        chatUserInfo.setText(profile.getStudyProgram() + " • Semester " + profile.getSemester());

        // Update avatar
        chatAvatar.getChildren().clear();
        if (profile.getImageUrls() != null && !profile.getImageUrls().isEmpty()) {
            try {
                Image image = new Image(profile.getImageUrls().get(0), 45, 45, true, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(45);
                imageView.setFitHeight(45);
                imageView.setPreserveRatio(false);

                // Create circular clip
                Rectangle clip = new Rectangle(45, 45);
                clip.setArcWidth(45);
                clip.setArcHeight(45);
                imageView.setClip(clip);

                chatAvatar.getChildren().add(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Hide empty state
        emptyChatState.setVisible(false);
        messagesScrollPane.setVisible(true);
        messageInput.setDisable(false);
        sendButton.setDisable(false);

        // Highlight selected chat
        updateSelectedChatStyle();

        // Load messages
        loadMessages();

        // Mark as read
        chatService.markMessagesAsRead(match.getId());

        // Start message refresh
        startMessageRefresh();
    }

    private void updateSelectedChatStyle() {
        // Remove selection from all items
        chatList.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                node.getStyleClass().remove("chat-item-selected");
            }
        });

        // Add selection to current item
        for (javafx.scene.Node node : chatList.getChildren()) {
            if (node instanceof HBox) {
                HBox item = (HBox) node;
                // Check if this is the selected chat by comparing profile names
                VBox textContainer = (VBox) item.getChildren().get(1);
                Label nameLabel = (Label) textContainer.getChildren().get(0);

                String selectedName = selectedProfile.getFirstName() + " " + selectedProfile.getLastName();
                if (nameLabel.getText().equals(selectedName)) {
                    item.getStyleClass().add("chat-item-selected");
                    break;
                }
            }
        }
    }

    private void loadMessages() {
        if (selectedMatch == null) return;

        chatService.getMessages(selectedMatch.getId())
                .thenAccept(messages -> {
                    Platform.runLater(() -> {
                        displayMessages(messages);
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        errorBox.showError("Failed to load messages.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void displayMessages(List<Message> messages) {
        messagesContainer.getChildren().clear();

        if (messages == null || messages.isEmpty()) {
            Label emptyLabel = new Label("No messages yet. Say hi!");
            emptyLabel.getStyleClass().add("empty-subtitle");
            messagesContainer.getChildren().add(emptyLabel);
            return;
        }

        String currentUserId = sessionManager.getCurrentUser().getId();

        for (Message message : messages) {
            boolean isSent = message.getSenderId().equals(currentUserId);
            HBox messageBox = createMessageBubble(message, isSent);
            messagesContainer.getChildren().add(messageBox);
        }

        // Scroll to bottom
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
        });
    }

    private HBox createMessageBubble(Message message, boolean isSent) {
        HBox container = new HBox();
        container.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.setPadding(new Insets(5, 0, 5, 0));

        VBox bubble = new VBox(5);
        bubble.setMaxWidth(300);
        bubble.getStyleClass().add("message-bubble");
        bubble.getStyleClass().add(isSent ? "message-sent" : "message-received");

        Label textLabel = new Label(message.getContent());
        textLabel.getStyleClass().add("message-text");
        textLabel.setWrapText(true);

        Label timeLabel = new Label(formatTime(message.getCreatedAt()));
        timeLabel.getStyleClass().add("message-time");

        bubble.getChildren().addAll(textLabel, timeLabel);
        container.getChildren().add(bubble);

        return container;
    }

    @FXML
    private void handleSendMessage() {
        if (selectedMatch == null || messageInput.getText().trim().isEmpty()) {
            return;
        }

        String content = messageInput.getText().trim();
        String receiverId = selectedProfile.getUserId();

        messageInput.clear();
        messageInput.setDisable(true);
        sendButton.setDisable(true);

        chatService.sendMessage(selectedMatch.getId(), receiverId, content)
                .thenAccept(message -> {
                    Platform.runLater(() -> {
                        messageInput.setDisable(false);
                        sendButton.setDisable(false);

                        if (message != null) {
                            // Reload messages to show the new one
                            loadMessages();
                        } else {
                            errorBox.showError("Failed to send message.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        messageInput.setDisable(false);
                        sendButton.setDisable(false);
                        errorBox.showError("Failed to send message.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void startMessageRefresh() {
        // Stop previous refresh thread if exists
        if (messageRefreshThread != null && messageRefreshThread.isAlive()) {
            messageRefreshThread.interrupt();
        }

        // Start new refresh thread
        messageRefreshThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted() && selectedMatch != null) {
                try {
                    Thread.sleep(3000); // Refresh every 3 seconds

                    if (selectedMatch != null) {
                        chatService.getMessages(selectedMatch.getId())
                                .thenAccept(messages -> {
                                    Platform.runLater(() -> {
                                        if (selectedMatch != null) {
                                            displayMessages(messages);
                                        }
                                    });
                                });
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        messageRefreshThread.setDaemon(true);
        messageRefreshThread.start();
    }

    public void selectChatByProfile(Profile profile) {
        if (matches == null || profile == null) return;
        
        // Find the match that contains this profile
        String currentUserId = sessionManager.getCurrentUser().getId();
        
        for (Match match : matches) {
            String matchedUserId = match.getUser1Id().equals(currentUserId) ?
                    match.getUser2Id() : match.getUser1Id();
            
            if (matchedUserId.equals(profile.getUserId())) {
                match.setMatchedProfile(profile);
                selectChat(match, profile);
                break;
            }
        }
    }

    private String formatTime(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();

            if (dateTime.toLocalDate().equals(now.toLocalDate())) {
                return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else if (dateTime.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                return "Yesterday";
            } else {
                return dateTime.format(DateTimeFormatter.ofPattern("MMM dd"));
            }
        } catch (Exception e) {
            return "";
        }
    }
}
