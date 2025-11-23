package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.services.AuthService;
import at.ac.hcw.campusconnect.services.SessionManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class LoginController {
    @FXML
    private ImageView logoImageView;
    @FXML
    private TextField emailField;
    @FXML
    private Button sendCodeButton;
    @FXML
    private Button verifyButton;
    @FXML
    private VBox otpContainer;
    @FXML
    private TextField otpField;
    @FXML
    private Label errorLabel;

    private AuthService authService;
    private boolean otpSent = false;

    @FXML
    public void initialize() {
        Image logo = new Image(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("at/ac/hcw/campusconnect/images/logo.png")));
        logoImageView.setImage(logo);
        authService = SessionManager.getInstance().getAuthService();
        otpContainer.setVisible(false);
        otpContainer.setManaged(false);
        // clear any errors when the user edits fields
        emailField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        otpField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }


    @FXML
    private void handleSendCode() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email address.", emailField);
            return;
        }
        sendCodeButton.setDisable(true);
        sendCodeButton.setText("Sending...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return authService.sendOTP(email);
            }
        };

        task.setOnSucceeded(e -> {
                if (task.getValue()) {
                otpSent = true;
                otpContainer.setVisible(true);
                otpContainer.setManaged(true);
                // allow resending after success and update label
                sendCodeButton.setDisable(false);
                sendCodeButton.setText("Resend Code");
                clearError();
                    // focus the otp field so user can type immediately
                    Platform.runLater(() -> otpField.requestFocus());
            } else {
                sendCodeButton.setDisable(false);
                sendCodeButton.setText("Send Login Code");
                showError("Failed to send verification code. Please try again.");
            }
        });

        task.setOnFailed(e -> {
            sendCodeButton.setDisable(false);
            sendCodeButton.setText("Send Login Code");
            showError("Network error. Please check your connection and try again.");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleVerifyCode() {
        if (!otpSent) {
            showError("Please send a verification code first.");
            return;
        }

        String email = emailField.getText().trim();
        String otp = otpField.getText().trim();

        if (otp.isEmpty()) {
            showError("Please enter the verification code.", otpField);
            return;
        }

        verifyButton.setDisable(true);
        verifyButton.setText("Verifying...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return authService.verifyOTP(email, otp);
            }
        };

        task.setOnSucceeded(e -> {
                if (task.getValue()) {
                if (authService.hasProfile()) {

                } else {
                    switchToView("profile-setup");
                }
            } else {
                verifyButton.setDisable(false);
                verifyButton.setText("Verify Code");
                    showError("Invalid verification code. Please try again.", otpField);
            }
        });

        task.setOnFailed(e -> {
            verifyButton.setDisable(false);
            verifyButton.setText("Verify Code");
            showError("Verification failed. Please try again.");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                System.err.println("Error: " + message);
            }
        });
    }

    private void showError(String message, TextField field) {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText(message);
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            } else {
                System.err.println("Error: " + message);
            }

            if (field != null) {
                if (!field.getStyleClass().contains("input-error")) {
                    field.getStyleClass().add("input-error");
                }
                field.requestFocus();
            }
        });
    }

    private void clearError() {
        Platform.runLater(() -> {
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
            if (emailField != null && emailField.getStyleClass().contains("input-error")) {
                emailField.getStyleClass().remove("input-error");
            }
            if (otpField != null && otpField.getStyleClass().contains("input-error")) {
                otpField.getStyleClass().remove("input-error");
            }
        });
    }

    //TODO: improve scene switching later
    private void switchToView(String viewName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/at/ac/hcw/campusconnect/" + viewName + ".fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/at/ac/hcw/campusconnect/styles/main.css")).toExternalForm()
            );
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}