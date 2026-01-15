package at.ac.hcw.campusconnect.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ErrorBox extends StackPane {
    private final HBox container;
    private final Label messageLabel;
    private boolean isShowing = false;

    public ErrorBox() {
        container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("error-box");
        container.setMaxWidth(500);
        container.setMinHeight(50);
        container.setVisible(false);
        container.setOpacity(0);

        messageLabel = new Label();
        messageLabel.getStyleClass().add("error-message");
        messageLabel.setWrapText(true);

        container.getChildren().add(messageLabel);
        getChildren().add(container);
        setAlignment(Pos.TOP_CENTER);
        setPickOnBounds(false);
    }

    public void showError(String message) {
        if (isShowing) {
            // If already showing, just update the message
            messageLabel.setText(message);
            return;
        }

        isShowing = true;
        messageLabel.setText(message);
        container.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> hide());
        pause.play();
    }

    public void hide() {
        if (!isShowing) return;

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            container.setVisible(false);
            isShowing = false;
        });
        fadeOut.play();
    }
}
