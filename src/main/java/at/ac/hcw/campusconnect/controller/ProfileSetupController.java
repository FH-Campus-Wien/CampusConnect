package at.ac.hcw.campusconnect.controller;

import at.ac.hcw.campusconnect.components.AutoCompleteTextField;
import at.ac.hcw.campusconnect.models.Profile;
import at.ac.hcw.campusconnect.services.ImageStorageService;
import at.ac.hcw.campusconnect.services.ProfileService;
import at.ac.hcw.campusconnect.services.SessionManager;
import at.ac.hcw.campusconnect.util.SceneNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ProfileSetupController {

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

    // Services
    private SessionManager sessionManager;
    private ProfileService profileService;
    private ImageStorageService imageStorageService;

    // Selected image files (max 4)
    private List<File> selectedImageFiles = new ArrayList<>();
    private List<ImageView> imagePreviewViews = new ArrayList<>();

    // Study programs by degree type
    private Map<String, List<String>> studyPrograms;

    // Selected interests
    private Set<String> selectedInterests = new HashSet<>();

    // Interest categories
    private Map<String, List<String>> interestCategories;

    public void initialize() {
        // Initialize services
        sessionManager = SessionManager.getInstance();
        profileService = new ProfileService(sessionManager);
        imageStorageService = new ImageStorageService(sessionManager);

        setupDegreeTypes();
        setupPronouns();
        setupGenderOptions();
        setupLookingForOptions();
        setupInterestedInOptions();
        setupStudyPrograms();
        setupSemesterOptions();
        setupInterests();

        // Set up listeners
        degreeTypeComboBox.setOnAction(e -> {
            updateSemesterOptions();
            updateStudyProgramOptions();
        });

        // Set up add image button
        addImageButton.setOnAction(e -> handleAddImage());
    }

    private void setupDegreeTypes() {
        degreeTypeComboBox.setItems(FXCollections.observableArrayList("Bachelor", "Master"));
    }

    private void setupPronouns() {
        ObservableList<String> pronounsList = FXCollections.observableArrayList(
                "she/her", "he/him", "they/them", "she/they", "he/they", "any pronouns", "ask me"
        );
        pronounsComboBox.setItems(pronounsList);
    }

    private void setupGenderOptions() {
        ObservableList<String> genderList = FXCollections.observableArrayList(
                "Woman", "Man", "Non-binary", "Genderfluid", "Other", "Prefer not to say"
        );
        genderComboBox.setItems(genderList);
    }

    private void setupLookingForOptions() {
        ObservableList<String> lookingForList = FXCollections.observableArrayList(
                "Friends", "Dating", "Both"
        );
        lookingForComboBox.setItems(lookingForList);
    }

    private void setupInterestedInOptions() {
        ObservableList<String> interestedInList = FXCollections.observableArrayList(
                "Women", "Men", "Non-binary people", "Everyone"
        );
        interestedInComboBox.setItems(interestedInList);
    }

    private void setupStudyPrograms() {
        studyPrograms = new HashMap<>();

        // Bachelor programs
        List<String> bachelorPrograms = Arrays.asList(
                "Biomedizinische Analytik (VZ)",
                "Diätologie (VZ)",
                "Ergotherapie (VZ)",
                "Hebammen (VZ)",
                "Logopädie – Phoniatrie – Audiologie (VZ)",
                "Orthoptik (VZ)",
                "Physiotherapie (VZ)",
                "Radiologietechnologie (VZ)",
                "Bioengineering (BB)",
                "Molekulare Biotechnologie (VZ)",
                "Nachhaltiges Ressourcenmanagement (BB)",
                "Verpackungstechnologie (BB)",
                "Architektur – Green Building (VZ)",
                "Bauingenieurwesen – Baumanagement (BB)",
                "Bauingenieurwesen – Baumanagement (VZ)",
                "Gesundheits- und Krankenpflege (VZ)",
                "Soziale Arbeit (BB)",
                "Soziale Arbeit (VZ)",
                "Sozialmanagement in der Elementarpädagogik (BB)",
                "Angewandte Elektronik (BB)",
                "Clinical Engineering (BB)",
                "Computer Science and Digital Communications (BB)",
                "Computer Science and Digital Communications (VZ)",
                "High Tech Manufacturing (VZ)",
                "Integriertes Sicherheitsmanagement (BB)",
                "Public Management (BB)",
                "Tax Management (BB)"
        );

        // Master programs
        List<String> masterPrograms = Arrays.asList(
                "Advanced Integrative Health Studies (BB)",
                "Biomedizinische Analytik (BB)",
                "Ganzheitliche Therapie und Salutogenese (BB)",
                "Health Assisting Engineering (BB)",
                "Radiologietechnologie (BB)",
                "Bioinformatik (BB)",
                "Biotechnologisches Qualitätsmanagement (BB)",
                "Bioverfahrenstechnik (BB)",
                "Molecular Biotechnology (VZ)",
                "Packaging Technology and Sustainability (BB)",
                "Architektur – Green Building (VZ)",
                "Bauingenieurwesen – Baumanagement (BB)",
                "Technische Gebäudeausstattung (BB)",
                "Advanced Nursing Counseling / Complementary Care (BB)",
                "Advanced Nursing Education (BB)",
                "Advanced Nursing Practice (BB)",
                "Kinder- und Familienzentrierte Soziale Arbeit (BB)",
                "Sozialraumorientierte und Klinische Soziale Arbeit (VZ)",
                "Sozialwirtschaft und Soziale Arbeit (BB)",
                "Embedded Systems Engineering (BB)",
                "Green Mobility (BB)",
                "Health Assisting Engineering (BB)",
                "High Tech Manufacturing (BB)",
                "IT-Security (BB)",
                "Safety and Systems Engineering (BB)",
                "Software Design and Engineering (BB)",
                "Technisches Management (BB)",
                "Führung, Politik und Management (BB)",
                "Integriertes Risikomanagement (BB)",
                "Public Management (BB)",
                "Tax Management (BB)"
        );

        studyPrograms.put("Bachelor", bachelorPrograms);
        studyPrograms.put("Master", masterPrograms);
    }

    private void setupSemesterOptions() {
        int maxSemester = 6;
        ObservableList<Integer> semesters = FXCollections.observableArrayList();
        for (int i = 1; i <= maxSemester; i++) {
            semesters.add(i);
        }
        semesterComboBox.setItems(semesters);
    }

    private void updateSemesterOptions() {
        String selectedDegree = degreeTypeComboBox.getValue();
        if (selectedDegree != null) {
            ObservableList<Integer> semesters = FXCollections.observableArrayList();
            int maxSemester = selectedDegree.equals("Bachelor") ? 6 : 4;
            for (int i = 1; i <= maxSemester; i++) {
                semesters.add(i);
            }
            semesterComboBox.setDisable(false); // Aktivieren der ComboBox
            semesterComboBox.setItems(semesters);
            semesterComboBox.setValue(null);

        } else {
            semesterComboBox.setDisable(true); // Deaktivieren der ComboBox, wenn kein Degree ausgewählt ist
        }
    }

    private void updateStudyProgramOptions() {
        String selectedDegree = degreeTypeComboBox.getValue();
        if (selectedDegree != null && studyPrograms.containsKey(selectedDegree)) {
            List<String> programs = studyPrograms.get(selectedDegree);
            studyProgramField.setDisable(false);
            studyProgramField.getEntries().clear();
            studyProgramField.getEntries().addAll(programs);
            studyProgramField.setText(""); // Clear text
        }
    }

    private void setupInterests() {
        interestCategories = new HashMap<>();

        // Academic Interests
        interestCategories.put("Academic", Arrays.asList(
                "Study Groups", "Research", "Tutoring", "Library Sessions", "Philosophy Discussions",
                "Academic Writing", "Presentations", "Science", "Language Learning"
        ));

        // Sports & Fitness
        interestCategories.put("Sports", Arrays.asList(
                "Football", "Basketball", "Tennis", "Swimming", "Running",
                "Gym", "Yoga", "Hiking", "Cycling", "Volleyball", "Dancing",
                "Martial Arts", "Climbing", "Skating", "Skiing", "Golf"
        ));

        // Hobbies & Creative
        interestCategories.put("Hobbies", Arrays.asList(
                "Photography", "Painting", "Music", "Guitar", "Piano", "Gardening",
                "Cooking", "Baking", "Reading", "Writing", "Crafting", "Theater"
        ));

        // Social & Entertainment
        interestCategories.put("Social", Arrays.asList(
                "Movies", "TV Shows", "Parties", "Concerts", "Festivals",
                "Night Out", "Board Games", "Karaoke", "Travel", "Volunteering",
                "Coffee Shops", "Shopping"
        ));

        // Technology & Gaming
        interestCategories.put("Tech", Arrays.asList(
                "Gaming", "Programming", "AI", "Web Development", "Mobile Apps", "Game Development", "UX/UI Design",
                "Cryptocurrency", "Hackathons", "Cybersecurity", "Robotics"
        ));

        populateInterestPanes();
    }

    private void populateInterestPanes() {
        populateInterestPane(academicInterestsPane, interestCategories.get("Academic"));
        populateInterestPane(sportsInterestsPane, interestCategories.get("Sports"));
        populateInterestPane(hobbiesInterestsPane, interestCategories.get("Hobbies"));
        populateInterestPane(socialInterestsPane, interestCategories.get("Social"));
        populateInterestPane(techInterestsPane, interestCategories.get("Tech"));
    }

    private void populateInterestPane(FlowPane pane, List<String> interests) {
        for (String interest : interests) {
            Button interestButton = new Button(interest);
            interestButton.getStyleClass().add("interest-tag");

            interestButton.setOnAction(e -> {
                if (selectedInterests.contains(interest)) {
                    selectedInterests.remove(interest);
                    interestButton.getStyleClass().remove("interest-tag-selected");
                    interestButton.getStyleClass().add("interest-tag");
                } else {
                    selectedInterests.add(interest);
                    interestButton.getStyleClass().remove("interest-tag");
                    interestButton.getStyleClass().add("interest-tag-selected");
                }
            });

            pane.getChildren().add(interestButton);
        }
    }


    @FXML
    private void handleAddImage() {
        // Check if max images reached
        if (selectedImageFiles.size() >= 4) {
            showAlert("You can only upload a maximum of 4 images.");
            return;
        }

        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        // Allow multiple selection
        List<File> files = fileChooser.showOpenMultipleDialog(addImageButton.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            int remainingSlots = 4 - selectedImageFiles.size();
            List<File> filesToAdd = files.subList(0, Math.min(files.size(), remainingSlots));

            if (files.size() > remainingSlots) {
                showAlert("Only " + remainingSlots + " image(s) added. Maximum 4 images allowed.");
            }

            for (File file : filesToAdd) {
                addImagePreview(file);
            }
        }
    }

    private void addImagePreview(File imageFile) {
        try {
            // Add to selected files
            selectedImageFiles.add(imageFile);

            // Create image preview container
            StackPane imageContainer = new StackPane();
            imageContainer.setPrefSize(120, 120);
            imageContainer.setMaxSize(120, 120);
            imageContainer.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 8;");

            // Create image view
            Image image = new Image(imageFile.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(120);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imagePreviewViews.add(imageView);

            // Create remove button with X symbol
            Button removeBtn = new Button("×");
            removeBtn.getStyleClass().add("image-remove-button");
            removeBtn.setOnAction(e -> removeImagePreview(imageContainer, imageFile, imageView));

            // Position remove button in top-right corner
            StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(removeBtn, new javafx.geometry.Insets(5, 5, 0, 0));

            // Add components to container
            imageContainer.getChildren().addAll(imageView, removeBtn);

            // Add to UI
            this.imageContainer.getChildren().add(imageContainer);

        } catch (Exception e) {
            showAlert("Failed to load image: " + imageFile.getName());
            e.printStackTrace();
        }
    }

    private void removeImagePreview(StackPane container, File imageFile, ImageView imageView) {
        selectedImageFiles.remove(imageFile);
        imagePreviewViews.remove(imageView);
        this.imageContainer.getChildren().remove(container);
    }

    @FXML
    private void handleSaveProfile() {
        // Validate profile
        if (!validateProfile()) {
            return;
        }

        // Disable save button to prevent double submission
        saveButton.setDisable(true);
        saveButton.setText("Saving...");

        // Create profile object
        Profile profile = new Profile();
        profile.setFirstName(firstNameField.getText().trim());
        profile.setLastName(lastNameField.getText().trim());
        profile.setBirthdate(birthdatePicker.getValue().toString()); // YYYY-MM-DD format
        profile.setGender(genderComboBox.getValue());
        profile.setPronouns(pronounsComboBox.getValue());
        profile.setDegreeType(degreeTypeComboBox.getValue());
        profile.setSemester(semesterComboBox.getValue());
        profile.setStudyProgram(studyProgramField.getText().trim());
        profile.setLookingFor(lookingForComboBox.getValue());
        profile.setInterestedIn(interestedInComboBox.getValue());
        profile.setBio(aboutMeArea.getText().trim());
        profile.setInterests(selectedInterests);

        // Upload images first (if any)
        CompletableFuture<List<String>> imageUploadFuture;
        if (!selectedImageFiles.isEmpty()) {
            imageUploadFuture = imageStorageService.uploadImages(selectedImageFiles);
        } else {
            imageUploadFuture = CompletableFuture.completedFuture(new ArrayList<>());
        }

        // Chain image upload with profile creation
        imageUploadFuture
                .thenCompose(imageUrls -> {
                    profile.setImageUrls(imageUrls);
                    return profileService.createProfile(profile);
                })
                .thenAccept(savedProfile -> {
                    Platform.runLater(() -> {
                        try {
                            // Navigate to main app
                            SceneNavigator.switchScene(saveButton, "main.fxml");
                        } catch (Exception e) {
                            showAlert("Failed to navigate to main app: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                })
                .exceptionally(error -> {
                    Platform.runLater(() -> {
                        saveButton.setDisable(false);
                        saveButton.setText("Save Profile");
                        showAlert("Failed to save profile: " + error.getMessage());
                        error.printStackTrace();
                    });
                    return null;
                });
    }

    public Set<String> getSelectedInterests() {
        return new HashSet<>(selectedInterests);
    }

    private boolean validateProfile() {
        // Validate first name
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            showAlert("Please enter your first name.");
            return false;
        }

        // Validate last name
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            showAlert("Please enter your last name.");
            return false;
        }

        // Validate birthdate
        if (birthdatePicker.getValue() == null) {
            showAlert("Please select your birthdate.");
            return false;
        }

        // Validate gender
        if (genderComboBox.getValue() == null) {
            showAlert("Please select your gender.");
            return false;
        }

        // Validate pronouns
        if (pronounsComboBox.getValue() == null) {
            showAlert("Please select your pronouns.");
            return false;
        }

        // Validate degree type
        if (degreeTypeComboBox.getValue() == null) {
            showAlert("Please select your degree type.");
            return false;
        }

        // Validate semester
        if (semesterComboBox.getValue() == null) {
            showAlert("Please select your semester.");
            return false;
        }

        // Validate study program
        if (studyProgramField.getText() == null || studyProgramField.getText().trim().isEmpty()) {
            showAlert("Please select your study program.");
            return false;
        }

        // Validate looking for
        if (lookingForComboBox.getValue() == null) {
            showAlert("Please select what you're looking for.");
            return false;
        }

        // Validate interested in
        if (interestedInComboBox.getValue() == null) {
            showAlert("Please select who you're interested in.");
            return false;
        }

        // Validate bio (optional but recommend some content)
        if (aboutMeArea.getText() == null || aboutMeArea.getText().trim().isEmpty()) {
            showAlert("Please write a short bio about yourself.");
            return false;
        }

        // Validate interests (minimum 3)
        if (selectedInterests.size() < 3) {
            showAlert("Please select at least 3 interests to help others connect with you.");
            return false;
        }

        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Profile Setup");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile Setup");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
