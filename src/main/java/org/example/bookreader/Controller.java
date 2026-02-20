package org.example.bookreader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class Controller {
    private javafx.animation.PauseTransition searchDelay;
    private AllBookController currentAllBooksController;

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
    private TextField searchField;

    private List<Book> bookList;
    @FXML
    private ImageView bookCoverView;

   @FXML
    public void initialize() throws IOException {
       currentAllBooksController = (AllBookController) loadPage("allbooks.fxml");
        bookList=  Library.loadBooks();
        sortByTitle();
        if (allBtn != null) {
            setActiveStyle(allBtn);
        }
       searchField.textProperty().addListener((obs, oldVal, newVal) -> {
           if (currentAllBooksController != null) {
               try {
                   currentAllBooksController.setFilter(newVal.trim().toLowerCase());
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           }

       });
    }

    @FXML
    public void changeToAllBooks() {
        currentAllBooksController = (AllBookController) loadPage("allbooks.fxml");
        setActiveStyle(allBtn);
        // AllBookController.initialize() → loadBooks() runs automatically
        // no need to call refreshBookGrid() here
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
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

    public void sortByTitle() throws IOException {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ▼");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
        if (currentAllBooksController != null)
            currentAllBooksController.setSort("title");

    }

    public void sortByAuthor() throws IOException {
        setActiveSort(authorLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        authorLabel.setText("Author ▼");
        progressLabel.setText("Progress");
        if (currentAllBooksController != null)
            currentAllBooksController.setSort("author");
    }

    public void sortByDate() throws IOException {
        setActiveSort(dateLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ▼");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
        if (currentAllBooksController != null)
            currentAllBooksController.setSort("date");
    }

    public void sortByProgress() throws IOException {
        setActiveSort(progressLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress ▼");
        if (currentAllBooksController != null)
            currentAllBooksController.setSort("progress");
    }

    public void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");
        active.getStyleClass().add("label-color-active");
    }



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

                String bookTitle = selectedFile.getName().replace(".pdf", "");


                TextInputDialog dialog = new TextInputDialog("General");
                dialog.setTitle("New Book Category");
                dialog.setHeaderText("Categorizing: " + bookTitle);

                String finalCategory = dialog.showAndWait().orElse("Uncategorized");


                PDFEngine engine = new PDFEngine(destination.getAbsolutePath());
                Image coverImage = engine.renderingPage(0);
                int totalPages = engine.getPageCount();
                String coverPath= saveCover(coverImage,bookTitle);
                engine.close();


                Book newBook = new Book(bookTitle, destination.getAbsolutePath(), totalPages, finalCategory,0.0,coverPath,0); // Simple cover path for now
                bookList.add(newBook);
                Library.saveBookList(bookList);
                refreshBookGrid();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // Helper method to save the JavaFX Image to a file
    private String saveCover(Image image, String title) {
        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        String path = "covers/" + safeTitle + ".png";
        File file = new File(path);
        file.getParentFile().mkdirs(); // Create the 'covers' folder if it doesn't exist
        try {
            // Convert JavaFX Image to standard BufferedImage so ImageIO can save it
            java.awt.image.BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
            javax.imageio.ImageIO.write(bImage, "png", file);
            return file.getPath(); // Return the path we saved it to!
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void refreshBookGrid() throws IOException {
        System.out.println("=== DEBUG: refreshBookGrid called ===");
        System.out.println("BookList size: " + bookList.size());

        javafx.scene.Node gridNode = contentArea.lookup("#bookGrid");
        System.out.println("Grid found: " + (gridNode != null));

        if (gridNode != null && gridNode instanceof javafx.scene.layout.FlowPane) {
            javafx.scene.layout.FlowPane bookGrid = (javafx.scene.layout.FlowPane) gridNode;
            bookGrid.getChildren().clear();

            for (Book book : bookList) {
                bookGrid.getChildren().add(createBookTile(book));
            }
            System.out.println("Grid refreshed with " + bookList.size() + " books.");
        } else {
            System.out.println("Grid not found in current view.");
        }
    }

    public VBox createBookTile(Book book) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/bookreader/book.fxml")
        );
        VBox card = loader.load();
        BookController ctrl = loader.getController();
        ctrl.setBook(book);
        //Fav
        Button favButton = new Button(book.isFavourite() ? "♥" : "♡");
        favButton.setStyle(book.isFavourite()
                ? "-fx-background-color: rgba(79,142,247,0.85); -fx-text-fill: white; -fx-background-radius: 50; -fx-cursor: hand;"
                : "-fx-background-color: rgba(30,33,48,0.75); -fx-text-fill: #4f8ef7; -fx-background-radius: 50; -fx-cursor: hand;");

        favButton.setOnAction(e -> {
            e.consume();
            book.setFavouriteStatus(!book.isFavourite());

            // find and update the matching book in bookList
            for (Book b : bookList) {
                if (b.getFilePath().equals(book.getFilePath())) {
                    b.setFavouriteStatus(book.isFavourite());
                    break;
                }
            }
            Library.saveBookList(bookList);

            // update button appearance
            favButton.setText(book.isFavourite() ? "♥" : "♡");
            favButton.setStyle(book.isFavourite()
                    ? "-fx-background-color: rgba(79,142,247,0.85); -fx-text-fill: white; " +
                    "-fx-background-radius: 50; -fx-cursor: hand;"
                    : "-fx-background-color: rgba(30,33,48,0.75); -fx-text-fill: #4f8ef7; " +
                    "-fx-background-radius: 50; -fx-cursor: hand;");
        });

// add favButton to your card somehow — depends on your book.fxml layout
// simplest way: add it to the card directly
        card.getChildren().add(favButton);

        // click handlers
        card.setOnMouseClicked(e -> {
            if (!e.getTarget().equals(ctrl.getDeleteButton())
                    && !e.getTarget().equals(favButton)
                    && !(e.getTarget() instanceof javafx.scene.text.Text
                    && ((javafx.scene.text.Text)e.getTarget()).getParent().equals(favButton)))
                openBook(book);
        });
        ctrl.getDeleteButton().setOnMouseClicked(e -> {
            try {
                deleteBook(book);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            e.consume();
        });

        return card;
    }

    public void deleteBook(Book bookToDelete) throws IOException {


        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete '" + bookToDelete.getTitle() + "'?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book?");


        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {


            bookList.remove(bookToDelete);


            Library.saveBookList(bookList);

            refreshBookGrid();

            System.out.println(bookToDelete.getTitle() + " has been permanently deleted.");
        }

    }


    private void openBook(Book book) {
        System.out.println("Opening book: " + book.getTitle());
        System.out.println("File path: " + book.getFilePath());

        try {

            PDFEngine engine = new PDFEngine(book.getFilePath());


            loadReaderScreen(book);


        } catch (Exception e) {
            System.err.println("Error opening book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadReaderScreen(Book book) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/readerWindow.fxml"));
            Parent readerPage = loader.load();


            BookController readerBrain = loader.getController();


            readerBrain.startSession(book);


            contentArea.getChildren().setAll(readerPage);

            AnchorPane.setTopAnchor(readerPage, 0.0);
            AnchorPane.setBottomAnchor(readerPage, 0.0);
            AnchorPane.setLeftAnchor(readerPage, 0.0);
            AnchorPane.setRightAnchor(readerPage, 0.0);

        } catch (Exception e) {
            System.err.println("ERROR: Failed to load PDF Reader Screen!");
            e.printStackTrace();
        }
    }

    //Search
    void search() throws IOException {
        String query=searchField.getText().toLowerCase();
        if(query.isEmpty())
        {
            refreshBookGrid();
            return;
        }
        ObservableList<Book> filteredBooks= FXCollections.observableArrayList();
        for(Book book:bookList)
        {
            if(book.getTitle().toLowerCase().contains(query) || book.getTitle().toLowerCase().contains(query))
            {
                filteredBooks.add(book);
            }
        }
        javafx.scene.Node gridNode = contentArea.lookup("#bookGrid");
        if (gridNode != null && gridNode instanceof javafx.scene.layout.FlowPane) {
            javafx.scene.layout.FlowPane bookGrid = (javafx.scene.layout.FlowPane) gridNode;
            bookGrid.getChildren().clear();
            for (Book book : filteredBooks) {
                try {
                    bookGrid.getChildren().add(createBookTile(book));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
