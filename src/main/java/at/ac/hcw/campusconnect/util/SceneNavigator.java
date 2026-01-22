package at.ac.hcw.campusconnect.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility class for consistent scene navigation and FXML loading throughout the application.
 * Automatically applies the main CSS stylesheet to all scenes.
 */
public class SceneNavigator {
    
    private static final String FXML_BASE_PATH = "/at/ac/hcw/campusconnect/";
    private static final String CSS_PATH = "/at/ac/hcw/campusconnect/styles/main.css";

    public static void switchScene(Node sourceNode, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        Scene scene = new Scene(loader.load());
        
        // Apply CSS
        String cssUrl = SceneNavigator.class.getResource(CSS_PATH).toExternalForm();
        scene.getStylesheets().add(cssUrl);
        
        // Get the stage and set the new scene
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setScene(scene);
    }

    public static void loadViewIntoContainer(StackPane containerPane, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        Parent view = loader.load();
        
        containerPane.getChildren().clear();
        containerPane.getChildren().add(view);
    }

}
