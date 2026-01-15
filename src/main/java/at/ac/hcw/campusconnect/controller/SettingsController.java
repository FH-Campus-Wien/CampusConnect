package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.components.AutoCompleteTextField;
import at.ac.hcw.campusconnect.components.ErrorBox;
import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.services.ImageStorageService;
import at.ac.hcw.campusconnect.services.ProfileService;
import at.ac.hcw.campusconnect.services.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SettingsController {

    @FXML
    private ErrorBox errorBox;
    @FXML
    private FlowPane imageContainer;
    @FXML
    private Button addImageButton;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker birthdatePicker;
    @FXML
    private ComboBox<String> degreeTypeComboBox;
    @FXML
    private ComboBox<Integer> semesterComboBox;
    @FXML
    private ComboBox<String> pronounsComboBox;
    @FXML
    private AutoCompleteTextField studyProgramField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private ComboBox<String> lookingForComboBox;
    @FXML
    private ComboBox<String> interestedInComboBox;
    @FXML
    private TextArea aboutMeArea;
    @FXML
    private FlowPane academicInterestsPane;
    @FXML
    private FlowPane sportsInterestsPane;
    @FXML
    private FlowPane hobbiesInterestsPane;
    @FXML
    private FlowPane socialInterestsPane;
    @FXML
    private FlowPane techInterestsPane;
    @FXML
    private Button saveButton;
    @FXML
    private ProgressIndicator loadingIndicator;

    private SessionManager sessionManager;
    private ProfileService profileService;
    private ImageStorageService imageStorageService;
    
    private Profile currentProfile;
    private List<File> selectedImageFiles = new ArrayList<>();
    private List<String> existingImageUrls = new ArrayList<>();
    private Set<String> selectedInterests = new HashSet<>();
    private Map<String, List<String>> studyPrograms;
    private Map<String, List<String>> interestCategories;

    public void initialize() {
        sessionManager = SessionManager.getInstance();
        profileService = new ProfileService(sessionManager);
        imageStorageService = new ImageStorageService(sessionManager);

        setupFormOptions();
        loadCurrentProfile();
    }

    private void setupFormOptions() {
        // Setup degree types
        degreeTypeComboBox.getItems().addAll("Bachelor", "Master");
        
        // Setup pronouns
        pronounsComboBox.getItems().addAll("he/him", "she/her", "they/them", "other", "prefer not to say");
        
        // Setup gender
        genderComboBox.getItems().addAll("Male", "Female", "Non-binary", "Other", "Prefer not to say");
        
        // Setup looking for
        lookingForComboBox.getItems().addAll("Friends", "Dates", "Both");
        
        // Setup interested in
        interestedInComboBox.getItems().addAll("Men", "Women", "Everyone");

        // Setup study programs
        setupStudyPrograms();
        
        // Setup semester options
        degreeTypeComboBox.setOnAction(e -> updateSemesterOptions());
        
        // Setup interests
        setupInterests();
    }

    private void setupStudyPrograms() {
        studyPrograms = new HashMap<>();
        
        List<String> bachelorPrograms = Arrays.asList(
            "Computer Science", "Software Engineering", "Business Informatics",
            "Data Science", "Information Technology", "Cybersecurity",
            "Business Administration", "Marketing", "Finance",
            "Mechanical Engineering", "Electrical Engineering", "Civil Engineering"
        );
        
        List<String> masterPrograms = Arrays.asList(
            "Computer Science (Master)", "Software Engineering (Master)", 
            "Data Science (Master)", "Artificial Intelligence",
            "Business Administration (Master)", "MBA",
            "Engineering Management"
        );
        
        studyPrograms.put("Bachelor", bachelorPrograms);
        studyPrograms.put("Master", masterPrograms);
        
        studyProgramField.getEntries().addAll(bachelorPrograms);
    }

    private void updateSemesterOptions() {
        String degreeType = degreeTypeComboBox.getValue();
        semesterComboBox.getItems().clear();
        
        if ("Bachelor".equals(degreeType)) {
            for (int i = 1; i <= 6; i++) {
                semesterComboBox.getItems().add(i);
            }
        } else if ("Master".equals(degreeType)) {
            for (int i = 1; i <= 4; i++) {
                semesterComboBox.getItems().add(i);
            }
        }
        
        // Update study programs
        if (degreeType != null && studyPrograms.containsKey(degreeType)) {
            studyProgramField.getEntries().clear();
            studyProgramField.getEntries().addAll(studyPrograms.get(degreeType));
        }
    }

    private void setupInterests() {
        interestCategories = new HashMap<>();
        
        interestCategories.put("Academic", Arrays.asList(
            "Research", "Study Groups", "Mathematics", "Science", "Technology", 
            "Literature", "Philosophy", "Languages"
        ));
        
        interestCategories.put("Sports", Arrays.asList(
            "Football", "Basketball", "Tennis", "Volleyball", "Swimming", 
            "Running", "Gym", "Yoga", "Cycling", "Hiking"
        ));
        
        interestCategories.put("Hobbies", Arrays.asList(
            "Photography", "Cooking", "Gaming", "Reading", "Music", 
            "Art", "Dancing", "Theater", "Movies", "Travel"
        ));
        
        interestCategories.put("Social", Arrays.asList(
            "Parties", "Coffee", "Concerts", "Volunteering", "Networking", 
            "Events", "Food", "Wine", "Beer", "Clubbing"
        ));
        
        interestCategories.put("Tech", Arrays.asList(
            "Programming", "AI/ML", "Web Development", "Mobile Apps", 
            "Blockchain", "IoT", "Robotics", "Cybersecurity"
        ));
        
        createInterestTags(academicInterestsPane, interestCategories.get("Academic"));
        createInterestTags(sportsInterestsPane, interestCategories.get("Sports"));
        createInterestTags(hobbiesInterestsPane, interestCategories.get("Hobbies"));
        createInterestTags(socialInterestsPane, interestCategories.get("Social"));
        createInterestTags(techInterestsPane, interestCategories.get("Tech"));
    }

    private void createInterestTags(FlowPane pane, List<String> interests) {
        for (String interest : interests) {
            Label tag = new Label(interest);
            tag.getStyleClass().add("interest-tag");
            tag.setOnMouseClicked(event -> toggleInterest(tag, interest));
            pane.getChildren().add(tag);
        }
    }

    private void toggleInterest(Label tag, String interest) {
        if (selectedInterests.contains(interest)) {
            selectedInterests.remove(interest);
            tag.getStyleClass().remove("interest-tag-selected");
            tag.getStyleClass().add("interest-tag");
        } else {
            selectedInterests.add(interest);
            tag.getStyleClass().remove("interest-tag");
            tag.getStyleClass().add("interest-tag-selected");
        }
    }

    private void loadCurrentProfile() {
        loadingIndicator.setVisible(true);
        
        profileService.getProfile(sessionManager.getCurrentUser().getId())
                .thenAccept(profile -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        
                        if (profile != null) {
                            currentProfile = profile;
                            populateForm(profile);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        loadingIndicator.setVisible(false);
                        errorBox.showError("Failed to load profile.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void populateForm(Profile profile) {
        firstNameField.setText(profile.getFirstName());
        lastNameField.setText(profile.getLastName());
        
        try {
            LocalDate birthdate = LocalDate.parse(profile.getBirthdate(), DateTimeFormatter.ISO_DATE);
            birthdatePicker.setValue(birthdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        genderComboBox.setValue(profile.getGender());
        pronounsComboBox.setValue(profile.getPronouns());
        degreeTypeComboBox.setValue(profile.getDegreeType());
        
        updateSemesterOptions();
        semesterComboBox.setValue(profile.getSemester());
        
        studyProgramField.setText(profile.getStudyProgram());
        lookingForComboBox.setValue(profile.getLookingFor());
        interestedInComboBox.setValue(profile.getInterestedIn());
        aboutMeArea.setText(profile.getBio());
        
        // Load interests
        if (profile.getInterests() != null) {
            selectedInterests.addAll(profile.getInterests());
            updateInterestTagsSelection();
        }
        
        // Load images
        if (profile.getImageUrls() != null) {
            existingImageUrls.addAll(profile.getImageUrls());
            displayExistingImages();
        }
    }

    private void updateInterestTagsSelection() {
        updatePaneInterestSelection(academicInterestsPane);
        updatePaneInterestSelection(sportsInterestsPane);
        updatePaneInterestSelection(hobbiesInterestsPane);
        updatePaneInterestSelection(socialInterestsPane);
        updatePaneInterestSelection(techInterestsPane);
    }

    private void updatePaneInterestSelection(FlowPane pane) {
        for (javafx.scene.Node node : pane.getChildren()) {
            if (node instanceof Label) {
                Label tag = (Label) node;
                String interest = tag.getText();
                
                if (selectedInterests.contains(interest)) {
                    tag.getStyleClass().remove("interest-tag");
                    tag.getStyleClass().add("interest-tag-selected");
                }
            }
        }
    }

    private void displayExistingImages() {
        imageContainer.getChildren().clear();
        
        for (String imageUrl : existingImageUrls) {
            StackPane container = new StackPane();
            container.setPrefSize(150, 150);
            container.getStyleClass().add("image-preview-container");
            
            ImageView imageView = new ImageView();
            imageView.setFitWidth(150);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(false);
            
            try {
                Image image = new Image(imageUrl, 150, 150, true, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            Button removeButton = new Button("Remove");
            removeButton.getStyleClass().add("remove-image-button");
            StackPane.setAlignment(removeButton, Pos.TOP_RIGHT);
            StackPane.setMargin(removeButton, new javafx.geometry.Insets(5));
            
            removeButton.setOnAction(e -> {
                existingImageUrls.remove(imageUrl);
                imageContainer.getChildren().remove(container);
            });
            
            container.getChildren().addAll(imageView, removeButton);
            imageContainer.getChildren().add(container);
        }
    }

    @FXML
    private void handleAddImage() {
        if (existingImageUrls.size() + selectedImageFiles.size() >= 4) {
            errorBox.showError("Maximum 4 images allowed");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        File selectedFile = fileChooser.showOpenDialog(addImageButton.getScene().getWindow());
        
        if (selectedFile != null) {
            selectedImageFiles.add(selectedFile);
            displayNewImage(selectedFile);
        }
    }

    private void displayNewImage(File file) {
        StackPane container = new StackPane();
        container.setPrefSize(150, 150);
        container.getStyleClass().add("image-preview-container");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(false);
        
        try {
            Image image = new Image(file.toURI().toString(), 150, 150, true, true);
            imageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("remove-image-button");
        StackPane.setAlignment(removeButton, Pos.TOP_RIGHT);
        StackPane.setMargin(removeButton, new javafx.geometry.Insets(5));
        
        removeButton.setOnAction(e -> {
            selectedImageFiles.remove(file);
            imageContainer.getChildren().remove(container);
        });
        
        container.getChildren().addAll(imageView, removeButton);
        imageContainer.getChildren().add(container);
    }

    @FXML
    private void handleSave() {
        // Validate
        if (!validateForm()) {
            return;
        }

        saveButton.setDisable(true);
        loadingIndicator.setVisible(true);

        // Upload new images if any
        if (!selectedImageFiles.isEmpty()) {
            uploadImagesAndSave();
        } else {
            saveProfile();
        }
    }

    private void uploadImagesAndSave() {
        imageStorageService.uploadImages(selectedImageFiles)
                .thenAccept(imageUrls -> {
                    Platform.runLater(() -> {
                        existingImageUrls.addAll(imageUrls);
                        selectedImageFiles.clear();
                        saveProfile();
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                        errorBox.showError("Failed to upload images. Please try again.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void saveProfile() {
        Profile updatedProfile = new Profile();
        updatedProfile.setId(currentProfile.getId());
        updatedProfile.setUserId(sessionManager.getCurrentUser().getId());
        updatedProfile.setFirstName(firstNameField.getText().trim());
        updatedProfile.setLastName(lastNameField.getText().trim());
        updatedProfile.setBirthdate(birthdatePicker.getValue().format(DateTimeFormatter.ISO_DATE));
        updatedProfile.setGender(genderComboBox.getValue());
        updatedProfile.setPronouns(pronounsComboBox.getValue());
        updatedProfile.setDegreeType(degreeTypeComboBox.getValue());
        updatedProfile.setSemester(semesterComboBox.getValue());
        updatedProfile.setStudyProgram(studyProgramField.getText().trim());
        updatedProfile.setLookingFor(lookingForComboBox.getValue());
        updatedProfile.setInterestedIn(interestedInComboBox.getValue());
        updatedProfile.setBio(aboutMeArea.getText().trim());
        updatedProfile.setInterests(selectedInterests);
        updatedProfile.setImageUrls(existingImageUrls);

        profileService.updateProfile(updatedProfile)
                .thenAccept(updatedProfileResult -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                        
                        if (updatedProfileResult != null) {
                            currentProfile = updatedProfileResult;
                            errorBox.showError("Profile updated successfully!");
                        } else {
                            errorBox.showError("Failed to update profile. Please try again.");
                        }
                    });
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                        errorBox.showError("Failed to update profile. Please try again.");
                    });
                    throwable.printStackTrace();
                    return null;
                });
    }

    @FXML
    private void handleCancel() {
        loadCurrentProfile();
    }

    private boolean validateForm() {
        if (firstNameField.getText().trim().isEmpty()) {
            errorBox.showError("Please enter your first name");
            return false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            errorBox.showError("Please enter your last name");
            return false;
        }
        
        if (birthdatePicker.getValue() == null) {
            errorBox.showError("Please select your birthdate");
            return false;
        }
        
        if (genderComboBox.getValue() == null) {
            errorBox.showError("Please select your gender");
            return false;
        }
        
        if (pronounsComboBox.getValue() == null) {
            errorBox.showError("Please select your pronouns");
            return false;
        }
        
        if (degreeTypeComboBox.getValue() == null) {
            errorBox.showError("Please select your degree type");
            return false;
        }
        
        if (semesterComboBox.getValue() == null) {
            errorBox.showError("Please select your semester");
            return false;
        }
        
        if (studyProgramField.getText().trim().isEmpty()) {
            errorBox.showError("Please select your study program");
            return false;
        }
        
        if (lookingForComboBox.getValue() == null) {
            errorBox.showError("Please select what you're looking for");
            return false;
        }
        
        if (interestedInComboBox.getValue() == null) {
            errorBox.showError("Please select who you're interested in");
            return false;
        }
        
        if (aboutMeArea.getText().trim().isEmpty()) {
            errorBox.showError("Please write something about yourself");
            return false;
        }
        
        if (selectedInterests.size() < 3) {
            errorBox.showError("Please select at least 3 interests");
            return false;
        }
        
        if (existingImageUrls.isEmpty() && selectedImageFiles.isEmpty()) {
            errorBox.showError("Please add at least one photo");
            return false;
        }
        
        return true;
    }
}
