package org.example.bookreader;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class Controller {

    @FXML private AnchorPane contentArea;

    //Buttons
    @FXML private Button allBtn;
    @FXML private Button favBtn;
    @FXML private Button catBtn;
    @FXML private Button setBtn;
    @FXML private Button helpBtn;

    //Sort Labels
    @FXML private Label titleLabel;
    @FXML private Label progressLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;

    // No @FXML here! Data is not a UI component.
// ObservableList tells the UI to refresh automatically when a book is added.
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    @FXML private ImageView bookCoverView;
    @FXML
    public void initialize() {
        loadPage("allbooks.fxml");
        sortByTitle();
        if (allBtn != null) {
            setActiveStyle(allBtn);
        } else {
            System.out.println("DEBUG: allBtn is still null. Check Scene Builder fx:id!");
        }
    }

    @FXML
    public void changeToAllBooks() {
        loadPage("allbooks.fxml");
        setActiveStyle(allBtn);
    }

    @FXML
    public void changetoFavourites() {
        loadPage("fav.fxml");
        setActiveStyle(favBtn);
    }

    public void changeToCategories()
    {
        loadPage("cat.fxml");
        setActiveStyle(catBtn);
    }

    private void setActiveStyle(Button clickedButton) {
        // 1. Remove "active" class from all buttons
        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        helpBtn.getStyleClass().remove("active");
        setBtn.getStyleClass().remove("active");

        // 2. Add "active" class to the one clicked
        clickedButton.getStyleClass().add("active");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);

            // Anchors to make it fill the space
            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        } catch (IOException e) {
            System.out.println("Error loading: " + fxmlFile);
            e.printStackTrace();
        }
    }


    public void sortByTitle()
    {
    setActiveSort(titleLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }
    public void sortByAuthor()
    {
    setActiveSort(authorLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }
    public void sortByDate()
    {
    setActiveSort(dateLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author ");
        progressLabel.setText("Progress");
    }
    public void sortByProgress()
    {
setActiveSort(progressLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress ");
    }
    public void setActiveSort(Label active)
    {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");

        active.getStyleClass().add("label-color-active");
    }
    @FXML
    //method for adding a book upon the "add" button click
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

                extractBookDetails(destination);
                changeToAllBooks();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    public void extractBookDetails(File file) {

        String fileName = file.getName();
        String bookTitle = fileName.substring(0, fileName.lastIndexOf("."));

        TextInputDialog dialog = new TextInputDialog("General");
        dialog.setTitle("New Book Category");
        dialog.setHeaderText("Categorizing: " + bookTitle);
        dialog.setContentText("Enter Category :");

        Optional<String> result = dialog.showAndWait();
        String finalCategory = result.orElse("Uncategorized"); // Default if they cancel


        try{



            PDFEngine engine = new PDFEngine(file.getAbsolutePath());
            Image coverImage = engine.getPageImage(0);

            String coverPath =saveCover(coverImage,bookTitle);
            int totalPages = engine.getPageCount();

            // Create and Save the Book object
            Book newBook = new Book(bookTitle,file.getAbsolutePath(),totalPages,finalCategory,coverPath);

            List<Book> library = Library.loadBooks();
            library.add(newBook);
            Library.saveBookList(library);

            engine.close(); // Clean up memory
            changeToAllBooks(); //refreeh the shelf

        } catch (IOException e) {
            System.err.println("Error processing PDF");
        }

    }
    public void openReader(Book book) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/book.fxml"));
            Parent root = loader.load();


            BookController readerController = loader.getController();


            readerController.startSession(book);


            contentArea.getChildren().setAll(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            System.err.println("Could not open Reader: " + e.getMessage());
        }
    }
    private String saveCover(Image image, String title) {
        String path = "covers/" + title.replaceAll(" ", "_") + ".png";
        File file = new File(path);
        file.getParentFile().mkdirs(); // Create folder if it doesn't exist
        try {
            java.awt.image.BufferedImage bImage = javafx.embed.swing.SwingFXUtils.fromFXImage(image, null);
            javax.imageio.ImageIO.write(bImage, "png", file);
            return file.getPath();
        } catch (IOException e) {
            return null;
        }
    }

}