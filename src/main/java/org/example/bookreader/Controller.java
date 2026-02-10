package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class Controller {

    @FXML
    private AnchorPane contentArea;

    //Buttons
    @FXML
    private Button allBtn;
    @FXML
    private Button favBtn;
    @FXML
    private Button catBtn;
    @FXML
    private Button setBtn;
    @FXML
    private Button helpBtn;

    //Sort Labels
    @FXML
    private Label titleLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label dateLabel;

    @FXML
    //Default Initialization
    public void initialize() {
        loadPage("allbooks.fxml");
        sortByTitle();
        if (allBtn != null) {
            setActiveStyle(allBtn);
        } else {
            System.out.println("DEBUG: allBtn is still null. Check Scene Builder fx:id!");
        }
    }

    //Changing Buttons
    @FXML
    public void changeToAllBooks() {
        loadPage("allbooks.fxml");
        setActiveStyle(allBtn);

        javafx.application.Platform.runLater(() -> {
            refreshBookGrid();
        });
    }

    @FXML
    public void changetoFavourites() {
        loadPage("fav.fxml");
        setActiveStyle(favBtn);
    }

    public void changeToCategories() {
        loadPage("cat.fxml");
        setActiveStyle(catBtn);
    }

    //Changing buttonStyle
    private void setActiveStyle(Button clickedButton) {

        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        helpBtn.getStyleClass().remove("active");
        setBtn.getStyleClass().remove("active");


        clickedButton.getStyleClass().add("active");
    }

    //Loading the button related page
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/" + fxmlFile));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);


            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        } catch (IOException e) {
            System.out.println("Error loading: " + fxmlFile);
        }
    }

    //Setting up sort styles
    public void sortByTitle() {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    public void sortByAuthor() {
        setActiveSort(authorLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    public void sortByDate() {
        setActiveSort(dateLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author ");
        progressLabel.setText("Progress");
    }

    public void sortByProgress() {
        setActiveSort(progressLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress ");
    }

    public void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");

        active.getStyleClass().add("label-color-active");
    }


    //AddBooks
    private java.util.List<Book> bookList = new java.util.ArrayList<>();

    @FXML
    public void onAddBookButtonClick() {
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Select a book (pdf):");
        filechooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File selectedFile = filechooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                File dir = new File("booksdata");
                if (!dir.exists()) dir.mkdirs();

                File destination = new File(dir, selectedFile.getName());
                java.nio.file.Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

                Book newBook = new Book(selectedFile.getName(), destination.getAbsolutePath());
                bookList.add(newBook);
                refreshBookGrid();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshBookGrid() {
        javafx.scene.Node gridNode = contentArea.lookup("#bookGrid");

        if (gridNode != null && gridNode instanceof javafx.scene.layout.FlowPane) {
            javafx.scene.layout.FlowPane bookGrid = (javafx.scene.layout.FlowPane) gridNode;
            bookGrid.getChildren().clear();

            for (Book book : bookList) {
                bookGrid.getChildren().add(createBookTile(book));
            }
            System.out.println("Grid refreshed with " + bookList.size() + " books.");
        } else {
            System.out.println("Grid not found in current view. It will refresh when All Books is opened.");
        }
    }

    private VBox createBookTile(Book book) {
        VBox tile = new VBox(10);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("book-card");
        Image image = new Image(getClass().getResourceAsStream("/org/example/bookreader/placeholder.png"));
        ImageView coverView = new ImageView(image);
        coverView.setFitWidth(100);
        coverView.setPreserveRatio(true);

        Label titleLbl = new Label(book.getTitle());
        titleLbl.getStyleClass().add("book-title");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(100);

        tile.getChildren().addAll(coverView, titleLbl);
        return tile;
    }
}