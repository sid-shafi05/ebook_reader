package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.embed.swing.SwingFXUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

public class Controller {

    // Base folder on Desktop where books and covers are stored
    private static final String BASE_DATA_PATH =
            System.getProperty("user.home") + "/Desktop/ebook_project_data";

    @FXML
    private AnchorPane contentArea;

    @FXML
    private VBox topBar;

    @FXML
    private TextField searchField;

    //Buttons
    @FXML
    private Button allBtn;
    @FXML
    private Button favBtn;
    @FXML
    private Button catBtn;
    @FXML
    private Button statsBtn;

    //Sort Labels
    @FXML
    private Label titleLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Label dateLabel;


    // the four pages — loaded once at startup, never reloaded
    private javafx.scene.layout.FlowPane bookGrid;   // allbooks page grid
    private javafx.scene.layout.FlowPane favGrid;    // favourites page grid
    private Parent allbooksPage;
    private Parent favPage;
    private Parent catPage;
    private Parent statsPage;

    private List<Book> bookList;

    private String currentQuery = "";//search bar query
    private boolean suppressSearch = false;

    @FXML
    public void initialize() {
        bookList = Library.loadBooks();

        // --- load all 4 pages ONCE ---
        try {
            FXMLLoader l1 = new FXMLLoader(getClass().getResource("allbooks.fxml"));
            allbooksPage = l1.load();
            bookGrid = (javafx.scene.layout.FlowPane) l1.getNamespace().get("bookGrid");
            anchorFill(allbooksPage);

            FXMLLoader l2 = new FXMLLoader(getClass().getResource("fav.fxml"));
            favPage = l2.load();
            favGrid = (javafx.scene.layout.FlowPane) l2.getNamespace().get("favGrid");
            anchorFill(favPage);

            FXMLLoader l3 = new FXMLLoader(getClass().getResource("cat.fxml"));
            catPage = l3.load();
            catPage.getProperties().put("controller", l3.getController());
            anchorFill(catPage);

            FXMLLoader l4 = new FXMLLoader(getClass().getResource("stats.fxml"));
            statsPage = l4.load();
            anchorFill(statsPage);

            contentArea.getChildren().addAll(allbooksPage, favPage, catPage, statsPage);
            sortByTitle();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // show library by default
        showPage(allbooksPage, true);
        setActiveStyle(allBtn);
        fillBookGrid(bookGrid, false);

        // wire search
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (suppressSearch) return;
                currentQuery = newVal.trim().toLowerCase();
                filterBookGrid(currentQuery);
            });
        }
    }

    // sets all four anchors to 0 so a page fills contentArea completely
    private void anchorFill(Parent page) {
        AnchorPane.setTopAnchor(page, 0.0);
        AnchorPane.setBottomAnchor(page, 0.0);
        AnchorPane.setLeftAnchor(page, 0.0);
        AnchorPane.setRightAnchor(page, 0.0);
    }

    // shows exactly one page, hides the other three
    private void showPage(Parent pageToShow, boolean showTopBarFlag) {
        allbooksPage.setVisible(false);  allbooksPage.setManaged(false);
        favPage.setVisible(false);       favPage.setManaged(false);
        catPage.setVisible(false);       catPage.setManaged(false);
        statsPage.setVisible(false);     statsPage.setManaged(false);

        pageToShow.setVisible(true);
        pageToShow.setManaged(true);
        showTopBar(showTopBarFlag);
    }

    @FXML
    public void changeToAllBooks() {
        showPage(allbooksPage, true);
        setActiveStyle(allBtn);
        clearSearch();
        fillBookGrid(bookGrid, false);
    }

    @FXML
    public void changetoFavourites() {
        showPage(favPage, false);
        setActiveStyle(favBtn);
        fillBookGrid(favGrid, true);
    }

    @FXML
    public void changeToCategories() {
        showPage(catPage, false);
        setActiveStyle(catBtn);

        // get the controller and reload every time
        CatController catCtrl = (CatController) catPage.getProperties().get("controller");
        if (catCtrl != null) catCtrl.loadCategories();
    }

    @FXML
    public void changeToStats() {
        showPage(statsPage, false);
        setActiveStyle(statsBtn);
    }

    // fills a FlowPane with book tiles. if favouriteOnly=true, only shows favourited books
    private void fillBookGrid(javafx.scene.layout.FlowPane grid, boolean favouriteOnly) {
        bookList = Library.loadBooks();
        grid.getChildren().clear();

        int count = 0;
        for (Book book : bookList) {
            if (favouriteOnly && !book.isFavourite()) continue;
            if (!favouriteOnly && !matchesQuery(book, currentQuery)) continue;
            grid.getChildren().add(createBookTile(book));
            count++;
        }

        if (count == 0) {
            javafx.scene.layout.VBox msg = new javafx.scene.layout.VBox(8);
            msg.setAlignment(javafx.geometry.Pos.CENTER);
            msg.setStyle("-fx-padding: 60;");
            Label icon = new Label(favouriteOnly ? "🤍" : "📚");
            icon.setStyle("-fx-font-size: 40px;");
            Label line = new Label(favouriteOnly
                ? "No favourites yet. Tap ♡ on any book card to add it here."
                : "No books found. Click '+ Add Book' to get started.");
            line.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");
            line.setWrapText(true);
            msg.getChildren().addAll(icon, line);
            grid.getChildren().add(msg);
        }
    }

    // called from Main.java on startup — bookGrid is already set in initialize()
    public void refreshBookGrid() {
        if (bookGrid != null) fillBookGrid(bookGrid, false);
    }

    // clears the search field and resets the query
    private void clearSearch() {
        currentQuery = "";
        if (searchField != null) {
            suppressSearch = true;
            searchField.clear();
            suppressSearch = false;
        }
    }

    // show or hide the top bar (Add Book, sort labels etc.)
    private void showTopBar(boolean show) {
        if (topBar != null) {
            topBar.setVisible(show);
            topBar.setManaged(show);
        }
    }

    private void setActiveStyle(Button clickedButton) {
        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        statsBtn.getStyleClass().remove("active");
        clickedButton.getStyleClass().add("active");
    }


    public void sortByTitle() {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ▼");
        dateLabel.setText("Date");
        progressLabel.setText("Progress");
        bookList.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        refreshBookGrid();
    }


    public void sortByDate() {
        setActiveSort(dateLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ▼");
        progressLabel.setText("Progress");
        // newest first
        bookList.sort((a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
        refreshBookGrid();
    }

    public void sortByProgress() {
        setActiveSort(progressLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        progressLabel.setText("Progress ▼");
        // highest progress first
        bookList.sort((a, b) -> Double.compare(b.getProgressValue(), a.getProgressValue()));
        refreshBookGrid();
    }

    public void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");
        active.getStyleClass().add("label-color-active");
    }

    //method for adding a book upon the "add" button click

    @FXML
    public void onAddBookButtonClick() {
        FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Select a book or comic:");
        filechooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Supported Files (PDF, CBZ)", "*.pdf", "*.cbz"),
            new FileChooser.ExtensionFilter("PDF Books", "*.pdf"),
            new FileChooser.ExtensionFilter("CBZ Comics", "*.cbz")
        );

        File selectedFile = filechooser.showOpenDialog(null);
        if (selectedFile == null) return;

        try {
            File dir = new File(BASE_DATA_PATH + "\\booksdata");
            if (!dir.exists()) dir.mkdirs();

            //change this code so that instead of relative path, an absolute path is created .

            File destination = new File(dir, selectedFile.getName());
            java.nio.file.Files.copy(selectedFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String fileName = selectedFile.getName();
            boolean isComic = fileName.toLowerCase().endsWith(".cbz");
            // strip extension for the title
            String bookTitle = fileName.replaceAll("(?i)\\.(pdf|cbz)$", "");

            // genre dropdown instead of typing manually
            List<String> genres = List.of(
                // Academic & Educational
                "Textbook", "Mathematics", "Science", "Physics", "Chemistry",
                "Engineering", "Programming", "Economics", "Philosophy", "Literature",
                // Fiction & Story
                "Novel", "Fiction", "Science Fiction", "Fantasy", "Adventure",
                "Action", "Mystery", "Thriller", "Horror", "Romance", "Psychological",
                "Historical Fiction", "Period Drama", "Dystopian", "Supernatural",
                // Non-Fiction
                "History", "Biography", "Autobiography", "Self-Help", "Psychology",
                "Politics", "Travel", "True Crime", "Science & Nature", "Religion",
                // Comics & Graphic
                "Comic / Manga", "Graphic Novel", "Superhero",
                // Misc
                "Other"
            );
            ChoiceDialog<String> genreDialog = new ChoiceDialog<>("Textbook", genres);
            genreDialog.setTitle("Add to Library");
            genreDialog.setHeaderText("Select a genre for:  " + bookTitle);
            genreDialog.setContentText("Genre:");
            // dark styling
            DialogPane dp = genreDialog.getDialogPane();
            dp.setStyle("-fx-background-color: #1e1e2e; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
            String finalCategory = genreDialog.showAndWait().orElse("Other");

            Image coverImage;
            int totalPages;

            if (isComic) {
                // use ComicEngine for CBZ
                ComicEngine comicEng = new ComicEngine(destination.getAbsolutePath());
                totalPages = comicEng.getTotalPages();
                coverImage = comicEng.getPage(0); // first page is the cover
            } else {
                // use PDFEngine for PDF
                PDFEngine pdfEng = new PDFEngine(destination.getAbsolutePath());
                totalPages = pdfEng.getPageCount();
                coverImage = pdfEng.renderingPage(0);
                pdfEng.close();
            }

            String coverPath = saveCover(coverImage, bookTitle);
            Book newBook = new Book(bookTitle, destination.getAbsolutePath(), totalPages, finalCategory, 0.0, coverPath, 0);
            bookList.add(newBook);
            Library.saveBookList(bookList);
            refreshBookGrid();

            CatController catCtrl = (CatController) catPage.getProperties().get("controller");
            if (catCtrl != null) catCtrl.loadCategories();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Helper method to save the JavaFX Image to a file
    private String saveCover(Image image, String title) {
        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        String path = BASE_DATA_PATH + "\\covers\\" + safeTitle + ".png";
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
    // filter book grid by search query — uses the pre-loaded bookGrid field
    private void filterBookGrid(String query) {
        if (bookGrid == null) return;
        bookList = Library.loadBooks();
        bookGrid.getChildren().clear();

        int shown = 0;
        for (Book book : bookList) {
            if (matchesQuery(book, query)) {
                bookGrid.getChildren().add(createBookTile(book));
                shown++;
            }
        }
        if (shown == 0) {
            showNoResultsMessage(bookGrid, query);
        }
    }

    // shows a friendly "no results" label inside the grid
    private void showNoResultsMessage(javafx.scene.layout.FlowPane grid, String query) {
        javafx.scene.layout.VBox msgBox = new javafx.scene.layout.VBox(8);
        msgBox.setAlignment(javafx.geometry.Pos.CENTER);
        msgBox.setStyle("-fx-padding: 60;");

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 40px;");

        Label line1 = new Label(query.isEmpty() ? "No books in your library yet." : "No results for  \"" + query + "\"");
        line1.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label line2 = new Label(query.isEmpty() ? "Click '+ Add Book' to get started." : "Try a different word, or check the category name.");
        line2.setStyle("-fx-text-fill: #777777; -fx-font-size: 12px;");

        msgBox.getChildren().addAll(icon, line1, line2);
        grid.getChildren().add(msgBox);
    }

    // decides if a book matches the search query — title only
    // underscores and hyphens in the title are treated as spaces
    // so typing "chemistry chang" matches "Chemistry_10th_Edition_Raymond_Chang"
    private boolean matchesQuery(Book book, String query) {
        if (query == null || query.isEmpty()) return true;

        // normalize title: _ and - become spaces, then lowercase
        String title = book.getTitle().toLowerCase().replace("_", " ").replace("-", " ");

        // normalize query the same way
        String normalizedQuery = query.toLowerCase().replace("_", " ").replace("-", " ");

        // every word the user typed must appear somewhere in the title
        String[] words = normalizedQuery.split("\\s+");
        for (String word : words) {
            if (!title.contains(word)) {
                return false;
            }
        }
        return true;
    }

   /* private VBox createBookTile(Book book) {
        VBox tile = new VBox(10);
        tile.setAlignment(Pos.CENTER);
        tile.getStyleClass().add("book-card");
        tile.setPrefWidth(200);
        tile.setPrefHeight(180);

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
        coverView.setFitWidth(180);
        coverView.setFitHeight(100);
        coverView.setPreserveRatio(false);

        // Title
        Label titleLbl = new Label(book.getTitle());
        titleLbl.getStyleClass().add("book-title");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(180);
        titleLbl.setMaxHeight(50);

        // Page count
        Label pagesLbl = new Label(book.getTotalPages() + " Pages");
        pagesLbl.setStyle("-fx-text-fill: #8b92a0; -fx-font-size: 10px;");

        //progress bar
        ProgressBar progBar = new ProgressBar();
        progBar.setProgress(book.getProgressValue());
        progBar.setMaxWidth(100);
        progBar.setMaxHeight(80);

        //a small circle button with a "X" inside it
        // 1. Create the visual Circle shape (The Button Body)
        Circle deleteShape = new Circle(10); // Radius of 10
        deleteShape.setFill(javafx.scene.paint.Color.RED);

// 2. Create the 'X' text
        Label deleteText = new Label("X");
        deleteText.setTextFill(javafx.scene.paint.Color.WHITE);
        deleteText.setAlignment(Pos.CENTER);
        deleteText.setStyle("-fx-font-size: 10pt;"); // Make the X visible

// 3. Create the Container (StackPane) to layer them: X goes ON TOP of the Circle
        StackPane deleteButton = new StackPane(deleteShape, deleteText);
        deleteButton.setPrefSize(20, 20); // Make the whole clickable area 20x20

// 4. Set the hover effect using CSS (This needs to be in your application.css file)
        deleteButton.getStyleClass().add("delete-button");

// 5. Wire the Click Action
        deleteButton.setOnMouseClicked(event -> {
            // Make sure you are passing the 'book' object correctly!
            deleteBook(book);
        });

// 6. Add the whole delete button to the VBox card
        tile.getChildren().add(deleteButton);
        tile.getStyleClass().add("delete-button");
        tile.getChildren().addAll(coverView, titleLbl, pagesLbl, progBar);
        return tile;
    }*/
   VBox createBookTile(Book book) {
       VBox tile = new VBox(6);
       tile.setAlignment(Pos.TOP_CENTER);
       tile.getStyleClass().add("book-card");
       tile.setStyle("-fx-padding: 10; -fx-background-color: #2d2d2d; -fx-background-radius: 8;");
       tile.setPrefSize(180, 270);

       // --- COVER IMAGE ---
       ImageView coverView = new ImageView();
       coverView.setFitWidth(150);
       coverView.setFitHeight(130);
       coverView.setPreserveRatio(true);
       coverView.setSmooth(true);

       boolean coverLoaded = false;
       if (book.getCoverPath() != null) {
           File imageFile = new File(book.getCoverPath());
           if (imageFile.exists()) {
               coverView.setImage(new Image(imageFile.toURI().toString()));
               coverLoaded = true;
           }
       }

       // cover area — dark background, image centered inside
       StackPane coverBox = new StackPane();
       coverBox.setPrefWidth(158);
       coverBox.setPrefHeight(135);
       coverBox.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 5;");

       if (coverLoaded) {
           coverBox.getChildren().add(coverView);
           StackPane.setAlignment(coverView, javafx.geometry.Pos.CENTER);
       } else {
           // no cover — show book emoji placeholder
           Label placeholder = new Label("📄");
           placeholder.setStyle("-fx-font-size: 40;");
           coverBox.getChildren().add(placeholder);
           StackPane.setAlignment(placeholder, javafx.geometry.Pos.CENTER);
       }

       // --- DELETE BUTTON — overlaid top-right ---
       StackPane deleteButton = new StackPane();
       deleteButton.setPrefSize(20, 20);
       deleteButton.getStyleClass().add("delete-button");
       Label deleteText = new Label("✕");
       deleteText.setTextFill(javafx.scene.paint.Color.WHITE);
       deleteText.setStyle("-fx-font-size: 9pt; -fx-font-weight: bold;");
       deleteButton.getChildren().add(deleteText);
       deleteButton.setOnMouseClicked(event -> {
           deleteBook(book);
           event.consume();
       });

       // --- FAVOURITE HEART BUTTON — overlaid top-left of cover ---
       Button heartBtn = new Button(book.isFavourite() ? "❤" : "♡");
       String heartActiveStyle =
           "-fx-background-color: #e8174a; -fx-background-radius: 12;" +
           "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6;" +
           "-fx-text-fill: white; -fx-font-weight: bold;";
       String heartInactiveStyle =
           "-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12;" +
           "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6;" +
           "-fx-text-fill: #555555; -fx-font-weight: bold;";
       heartBtn.setStyle(book.isFavourite() ? heartActiveStyle : heartInactiveStyle);

       heartBtn.setOnMouseClicked(event -> {
           book.setFavouriteStatus(!book.isFavourite());
           heartBtn.setText(book.isFavourite() ? "❤" : "♡");
           heartBtn.setStyle(book.isFavourite() ? heartActiveStyle : heartInactiveStyle);

           // save to disk
           List<Book> library = Library.loadBooks();
           for (Book b : library) {
               if (b.getFilePath().equals(book.getFilePath())) {
                   b.setFavouriteStatus(book.isFavourite());
               }
           }
           Library.saveBookList(library);
           event.consume();
       });

       // wrap coverBox + delete + heart in AnchorPane so they sit exactly on corners
       AnchorPane coverWrapper = new AnchorPane();
       coverWrapper.setPrefWidth(158);
       coverWrapper.setPrefHeight(135);
       AnchorPane.setTopAnchor(coverBox, 0.0);
       AnchorPane.setLeftAnchor(coverBox, 0.0);
       AnchorPane.setRightAnchor(coverBox, 0.0);
       AnchorPane.setBottomAnchor(coverBox, 0.0);
       AnchorPane.setTopAnchor(deleteButton, 3.0);
       AnchorPane.setRightAnchor(deleteButton, 3.0);
       AnchorPane.setTopAnchor(heartBtn, 3.0);
       AnchorPane.setLeftAnchor(heartBtn, 3.0);
       coverWrapper.getChildren().addAll(coverBox, heartBtn, deleteButton);

       // click anywhere on tile except delete/heart button → open book
       tile.setOnMouseClicked(event -> {
           if (!deleteButton.isHover() && !heartBtn.isHover()) {
               openBook(book);
           }
       });

       // --- LABELS ---
       Label titleLbl = new Label(book.getTitle());
       titleLbl.getStyleClass().add("book-title");
       titleLbl.setWrapText(true);
       titleLbl.setMaxWidth(155);
       titleLbl.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 11px; -fx-font-weight: bold;");

       Label dateAddedLbl = new Label();
       if (book.getDateAdded() > 0) {
           java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy");
           dateAddedLbl.setText("Added: " + sdf.format(new java.util.Date(book.getDateAdded())));
       } else {
           dateAddedLbl.setText("");
       }
       dateAddedLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 9px;");

       Label pagesLbl = new Label(book.getTotalPages() + " Pages");
       pagesLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px;");

       // progress percentage text
       int progressPercent = (int)(book.getProgressValue() * 100);
       Label progressLbl = new Label(progressPercent + "% read");
       progressLbl.setStyle("-fx-text-fill: #39FF14; -fx-font-size: 9px; -fx-font-weight: bold;");

       ProgressBar progBar = new ProgressBar(book.getProgressValue());
       progBar.setPrefWidth(155);
       progBar.setPrefHeight(8);
       // explicit track + fill styling so it's always visible regardless of theme
       progBar.setStyle(
           "-fx-accent: #39FF14;" +
           "-fx-control-inner-background: #444444;" +
           "-fx-background-color: #444444;" +
           "-fx-background-radius: 4;" +
           "-fx-padding: 0;"
       );

       tile.getChildren().addAll(coverWrapper, titleLbl, dateAddedLbl, pagesLbl, progressLbl, progBar);
       return tile;
   }

    public void deleteBook(Book bookToDelete) {

        // 1. CONFIRMATION: Ask the user "Are you sure?" (Best practice)
        // We use JavaFX Alert for a native look
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete '" + bookToDelete.getTitle() + "'?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book?");

        // Wait for the user to click YES or NO
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {

            // 2. LOGIC: Remove the object from the live list
            // This is why using ObservableList is better - it auto-updates the UI!
            bookList.remove(bookToDelete);

            // 3. PERSISTENCE: Save the new, smaller list back to the JSON file
            Library.saveBookList(bookList); // Assuming LibraryData is your save utility

            // 4. UI REFRESH: Force the shelf to redraw without the deleted book
            refreshBookGrid();

            System.out.println(bookToDelete.getTitle() + " has been permanently deleted.");
        }
        // If the user clicks NO, nothing happens, and we quietly exit.
    }

    private void openBook(Book book) {
        try {
            loadReaderScreen(book);
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
            // Load the Reader FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/readerWindow.fxml"));
            Parent readerPage = loader.load();

            // Get the controller
            BookController readerBrain = loader.getController();

            // Start reading session
            readerBrain.startSession(book);

            // Create a NEW WINDOW for the reader
            javafx.stage.Stage readerStage = new javafx.stage.Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(readerPage, 1000, 700);

            String css = this.getClass().getResource("/org/example/bookreader/application.css").toExternalForm();
            scene.getStylesheets().add(css);

            readerStage.setTitle(book.getTitle() + " - StackShelf Reader");
            readerStage.setScene(scene);
            readerStage.setMaximized(true); // full screen — page fills available space
            readerStage.setOnHidden(e -> refreshBookGrid());

            readerStage.show();

        } catch (Exception e) {
            System.err.println("Error opening reader: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

