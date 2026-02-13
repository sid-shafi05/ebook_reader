package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Controller {

    @FXML private AnchorPane contentArea;
    @FXML private Button allBtn;
    @FXML private Button favBtn;
    @FXML private Button catBtn;
    @FXML private Button setBtn;
    @FXML private Button helpBtn;
    @FXML private Button addBtn;
    @FXML private Label titleLabel;
    @FXML private Label progressLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;

    private AllBooksController currentAllBooksController;

    @FXML
    public void initialize() {
        System.out.println("Controller initialized");
        changeToAllBooks();
        sortByTitle();
    }

    @FXML
    public void changeToAllBooks() {
        currentAllBooksController = (AllBooksController) loadPage("allbooks.fxml");
        setActiveStyle(allBtn);
    }

    @FXML
    public void changetoFavourites() {
        loadPage("fav.fxml");
        setActiveStyle(favBtn);
    }

    @FXML
    public void changeToCategories() {
        loadPage("cat.fxml");
        setActiveStyle(catBtn);
    }

    private void setActiveStyle(Button clickedButton) {
        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        helpBtn.getStyleClass().remove("active");
        setBtn.getStyleClass().remove("active");
        clickedButton.getStyleClass().add("active");
    }

    private Object loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/bookreader/" + fxmlFile)
            );
            Parent page = loader.load();

            contentArea.getChildren().setAll(page);

            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);

            return loader.getController();

        } catch (IOException e) {
            System.out.println("Error loading: " + fxmlFile);
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void sortByTitle() {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ↓");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    @FXML
    public void sortByAuthor() {
        setActiveSort(authorLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        authorLabel.setText("Author ↓");
        progressLabel.setText("Progress");
    }

    @FXML
    public void sortByDate() {
        setActiveSort(dateLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ↓");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    @FXML
    public void sortByProgress() {
        setActiveSort(progressLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress ↓");
    }

    private void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");
        active.getStyleClass().add("label-color-active");
    }

    @FXML
    public void onAddBookButtonClick() {
        System.out.println("Add Book button clicked!");

        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Select a book");
        filechooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Supported Files", "*.pdf", "*.cbz", "*.zip"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Comic Files", "*.cbz", "*.zip")
        );

        Window window = contentArea.getScene().getWindow();
        File selectedFile = filechooser.showOpenDialog(window);

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getName());

            try {
                // Create booksdata directory
                File dir = new File("booksdata");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                // Copy file
                File destination = new File(dir, selectedFile.getName());
                Files.copy(selectedFile.toPath(), destination.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                // Extract title
                String title = selectedFile.getName().replaceFirst("[.][^.]+$", "");

                // Use FileTypeManager to get pages and cover
                FileTypeManager ftm = new FileTypeManager();
                ftm.fileType(destination.getAbsolutePath());

                int totalPages = ftm.getTotalPage();
                Image coverImage = ftm.getPage(0);

                // Create book object
                Book newBook = new Book(title, destination.getAbsolutePath(), coverImage, totalPages);

                // Add to database
                BookDatabase.getInstance().addBook(newBook);

                System.out.println("Book added successfully!");
                System.out.println("Title: " + title);
                System.out.println("Pages: " + totalPages);

                // Refresh view
                if (currentAllBooksController != null) {
                    currentAllBooksController.refreshBooks();
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error adding book: " + e.getMessage());
            }
        }
    }
}