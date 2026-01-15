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
    
    /**
     * Switches to a new scene by replacing the entire window content.
     * Automatically applies the main CSS stylesheet.
     * 
     * @param sourceNode Any node from the current scene (to get the current window)
     * @param fxmlFileName The FXML file name (without path, e.g., "login.fxml" or "main.fxml")
     * @throws IOException if the FXML file cannot be loaded
     */
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
    
    /**
     * Switches to a new scene and returns the controller for further initialization.
     * Automatically applies the main CSS stylesheet.
     * 
     * @param sourceNode Any node from the current scene (to get the current window)
     * @param fxmlFileName The FXML file name (without path, e.g., "login.fxml" or "main.fxml")
     * @param <T> The controller type
     * @return The controller instance
     * @throws IOException if the FXML file cannot be loaded
     */
    public static <T> T switchSceneWithController(Node sourceNode, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        Scene scene = new Scene(loader.load());
        
        // Apply CSS
        String cssUrl = SceneNavigator.class.getResource(CSS_PATH).toExternalForm();
        scene.getStylesheets().add(cssUrl);
        
        // Get the stage and set the new scene
        Stage stage = (Stage) sourceNode.getScene().getWindow();
        stage.setScene(scene);
        
        return loader.getController();
    }
    
    /**
     * Loads an FXML view into a container without changing the scene.
     * Useful for loading content into the main app's content area.
     * 
     * @param containerPane The container to load the view into (typically a StackPane)
     * @param fxmlFileName The FXML file name (without path, e.g., "discover.fxml" or "chats.fxml")
     * @throws IOException if the FXML file cannot be loaded
     */
    public static void loadViewIntoContainer(StackPane containerPane, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        Parent view = loader.load();
        
        containerPane.getChildren().clear();
        containerPane.getChildren().add(view);
    }
    
    /**
     * Loads an FXML view into a container and returns the controller.
     * Useful for loading content into the main app's content area when you need the controller.
     * 
     * @param containerPane The container to load the view into (typically a StackPane)
     * @param fxmlFileName The FXML file name (without path, e.g., "discover.fxml" or "chats.fxml")
     * @param <T> The controller type
     * @return The controller instance
     * @throws IOException if the FXML file cannot be loaded
     */
    public static <T> T loadViewIntoContainerWithController(StackPane containerPane, String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        Parent view = loader.load();
        
        containerPane.getChildren().clear();
        containerPane.getChildren().add(view);
        
        return loader.getController();
    }
    
    /**
     * Loads an FXML file and returns just the root node.
     * Does not change any scenes or containers.
     * 
     * @param fxmlFileName The FXML file name (without path)
     * @return The loaded root node
     * @throws IOException if the FXML file cannot be loaded
     */
    public static Parent loadFxml(String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        return loader.load();
    }
    
    /**
     * Loads an FXML file and returns both the root node and controller.
     * Does not change any scenes or containers.
     * 
     * @param fxmlFileName The FXML file name (without path)
     * @param <T> The controller type
     * @return A LoadResult containing both the view and controller
     * @throws IOException if the FXML file cannot be loaded
     */
    public static <T> LoadResult<T> loadFxmlWithController(String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(FXML_BASE_PATH + fxmlFileName));
        Parent view = loader.load();
        T controller = loader.getController();
        return new LoadResult<>(view, controller);
    }
    
    /**
     * Result class containing both a view and its controller.
     */
    public static class LoadResult<T> {
        private final Parent view;
        private final T controller;
        
        public LoadResult(Parent view, T controller) {
            this.view = view;
            this.controller = controller;
        }
        
        public Parent getView() {
            return view;
        }
        
        public T getController() {
            return controller;
        }
    }
}
