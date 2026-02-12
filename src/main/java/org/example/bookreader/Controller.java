package org.example.bookreader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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


    // No @FXML here! Data is not a UI component.
// ObservableList tells the UI to refresh automatically when a book is added.
   // private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private List<Book> bookList;
    @FXML
    private ImageView bookCoverView;

    @FXML
    public void initialize() {
        loadPage("allbooks.fxml");
        bookList=  Library.loadBooks();
        sortByTitle();
        if (allBtn != null) {
            setActiveStyle(allBtn);
        }
    }

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

    private void setActiveStyle(Button clickedButton) {
        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        helpBtn.getStyleClass().remove("active");
        setBtn.getStyleClass().remove("active");
        clickedButton.getStyleClass().add("active");
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);


            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        } catch (IOException e) {
            System.out.println("Error loading: " + fxmlFile);
            e.printStackTrace();
        }
    }

    public void sortByTitle() {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ▼");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    public void sortByAuthor() {
        setActiveSort(authorLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        authorLabel.setText("Author ▼");
        progressLabel.setText("Progress");
    }

    public void sortByDate() {
        setActiveSort(dateLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ▼");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    public void sortByProgress() {
        setActiveSort(progressLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress ▼");
    }

    public void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");
        active.getStyleClass().add("label-color-active");
    }

    //method for adding a book upon the "add" button click

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

                // Get Category from User
                TextInputDialog dialog = new TextInputDialog("General");
                dialog.setTitle("New Book Category");
                dialog.setHeaderText("Categorizing: " + bookTitle);
                String finalCategory = dialog.showAndWait().orElse("Uncategorized");

                // Use PDF Engine
                PDFEngine engine = new PDFEngine(destination.getAbsolutePath());
                Image coverImage = engine.renderingPage(0);
                int totalPages = engine.getPageCount();
                String coverPath= saveCover(coverImage,bookTitle);
                engine.close();

                // 3. Create the Book object (Using the full constructor)
                Book newBook = new Book(bookTitle, destination.getAbsolutePath(), totalPages, finalCategory,0.0,coverPath); // Simple cover path for now
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

    // Extract first page of PDF as cover image
   /* private Image extractPDFCover(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(0, 150);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            System.err.println("Error extracting PDF cover: " + e.getMessage());
            return null;
        }
    }

    // Extract total number of pages from PDF
    private int extractTotalPages(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            int pageCount = document.getNumberOfPages();
            System.out.println("Extracted " + pageCount + " pages from " + pdfFile.getName());
            return pageCount;
        } catch (IOException e) {
            System.err.println("Error extracting page count: " + e.getMessage());
            return 0;
        }
    }
*/
    private void refreshBookGrid() {
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

    private VBox createBookTile(Book book) {
        VBox tile = new VBox(10);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("book-card");
        tile.setPrefWidth(200);
        tile.setPrefHeight(200);

        // Make the tile clickable
        tile.setOnMouseClicked(event -> {
            openBook(book);
        });

        // Cover image
        ImageView coverView = new ImageView();
        if (book.getCoverPath() != null) {
            File imageFile = new File(book.getCoverPath());
            if (imageFile.exists()) {
                // This converts the saved path string back into a displayable Image object
                Image coverImage = new Image(imageFile.toURI().toString());
                coverView.setImage(coverImage);
            }
        }
        coverView.setFitWidth(340);
        coverView.setFitHeight(300);
        coverView.setPreserveRatio(false);

        // Title
        Label titleLbl = new Label(book.getTitle());
        titleLbl.getStyleClass().add("book-title");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(100);

        // Page count
        Label pagesLbl = new Label(book.getTotalPages() + " Pages");
        pagesLbl.setStyle("-fx-text-fill: #8b92a0; -fx-font-size: 10px;");

        //progress bar
        ProgressBar progBar = new ProgressBar();
        progBar.setProgress(book.getProgressValue());
        progBar.setMaxWidth(100);

        tile.getChildren().addAll(coverView, titleLbl, pagesLbl, progBar);
        return tile;
    }

    //open the book upon clicking on a book card
    private void openBook(Book book) {
        System.out.println("Opening book: " + book.getTitle());
        System.out.println("File path: " + book.getFilePath());

        try {
            // Use your PDFEngine to open the book
            PDFEngine engine = new PDFEngine(book.getFilePath());

            // You can either:
            // Option 1: Open in a new window with your PDF reader
            //openPDFReaderWindow(book);
            loadReaderScreen(book);

            // Option 2: Just open with system default PDF viewer
            // openWithSystemViewer(book);

        } catch (Exception e) {
            System.err.println("Error opening book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Option 1: Open in your custom PDF reader window
    /*private void openPDFReaderWindow(Book book) throws IOException {
        // Create a new stage (window) for reading
        javafx.stage.Stage readerStage = new javafx.stage.Stage();

        // Load your PDF reader FXML (you'll need to create this)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/readerWindow.fxml"));
        javafx.scene.Parent root = loader.load();

        // Pass the book to the reader controller
         BookController controller = loader.getController();
        //controller.loadBook(book, engine);
        controller.startSession(book);

        javafx.scene.Scene scene = new javafx.scene.Scene(root, 900, 700);
        readerStage.setTitle(book.getTitle());
        readerStage.setScene(scene);
        readerStage.show();
    }*/

    // Option 2: Open with system default PDF viewer
   /* private void openWithSystemViewer(Book book) {
        try {
            File pdfFile = new File(book.getFilePath());
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(pdfFile);
            }
        } catch (IOException e) {
            System.err.println("Could not open PDF: " + e.getMessage());
        }
    }*/
    // REPLACE the old openPDFReaderWindow with this new method:
    public void loadReaderScreen(Book book) {
        try {
            // 1. Load the Reader FXML into the content area
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/readerWindow.fxml"));
            Parent readerPage = loader.load();

            // 2. Get the Brain of the Reader Screen
            BookController readerBrain = loader.getController();

            // 3. Start the Engine and load the PDF
            readerBrain.startSession(book);

            // 4. SWAP THE SCREEN: Put the reader's FXML into the main window's content area
            contentArea.getChildren().setAll(readerPage);

            // 5. Make the reader fill the entire available space (very modern look)
            AnchorPane.setTopAnchor(readerPage, 0.0);
            AnchorPane.setBottomAnchor(readerPage, 0.0);
            AnchorPane.setLeftAnchor(readerPage, 0.0);
            AnchorPane.setRightAnchor(readerPage, 0.0);

        } catch (Exception e) {
            System.err.println("ERROR: Failed to load PDF Reader Screen!");
            e.printStackTrace();
        }
    }
}
    // Add this new method to BookController.java

