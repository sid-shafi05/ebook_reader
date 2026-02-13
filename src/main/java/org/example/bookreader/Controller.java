package org.example.bookreader;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @FXML private TextField searchField;

    private AllBooksController currentAllBooksController;
    private String currentSort = "title";

    @FXML
    public void initialize() {
        System.out.println("Controller initialized");
        changeToAllBooks();
        sortByTitle();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, o, n) -> {
                if (currentAllBooksController != null)
                    currentAllBooksController.filterBooks(n.trim().toLowerCase());
            });
        }
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML
    public void changeToAllBooks() {
        currentAllBooksController = (AllBooksController) loadPage("allbooks.fxml");
        if (currentAllBooksController != null) currentAllBooksController.setSort(currentSort);
        setActiveStyle(allBtn);
    }

    @FXML
    public void changetoFavourites() {
        loadPage("fav.fxml");
        currentAllBooksController = null;
        setActiveStyle(favBtn);
    }

    @FXML
    public void changeToCategories() {
        loadPage("cat.fxml");
        currentAllBooksController = null;
        setActiveStyle(catBtn);
    }

    private void setActiveStyle(Button btn) {
        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        helpBtn.getStyleClass().remove("active");
        setBtn.getStyleClass().remove("active");
        btn.getStyleClass().add("active");
    }

    private Object loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlFile));
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

    // ── Sort ──────────────────────────────────────────────────────────────────

    @FXML public void sortByTitle() {
        currentSort = "title";
        setActiveSort(titleLabel);
        titleLabel.setText("Title ▼"); dateLabel.setText("Date");
        authorLabel.setText("Author");  progressLabel.setText("Progress");
        if (currentAllBooksController != null) currentAllBooksController.setSort("title");
    }

    @FXML public void sortByAuthor() {
        currentSort = "author";
        setActiveSort(authorLabel);
        titleLabel.setText("Title");    dateLabel.setText("Date");
        authorLabel.setText("Author ▼"); progressLabel.setText("Progress");
        if (currentAllBooksController != null) currentAllBooksController.setSort("author");
    }

    @FXML public void sortByDate() {
        currentSort = "date";
        setActiveSort(dateLabel);
        titleLabel.setText("Title");  dateLabel.setText("Date ▼");
        authorLabel.setText("Author"); progressLabel.setText("Progress");
        if (currentAllBooksController != null) currentAllBooksController.setSort("date");
    }

    @FXML public void sortByProgress() {
        currentSort = "progress";
        setActiveSort(progressLabel);
        titleLabel.setText("Title");    dateLabel.setText("Date");
        authorLabel.setText("Author");   progressLabel.setText("Progress ▼");
        if (currentAllBooksController != null) currentAllBooksController.setSort("progress");
    }

    private void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");
        active.getStyleClass().add("label-color-active");
    }

    // ── Add Book ──────────────────────────────────────────────────────────────

    @FXML
    public void onAddBookButtonClick() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select a Book");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Supported", "*.pdf", "*.cbz", "*.zip"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Comic Files", "*.cbz", "*.zip")
        );

        Window window = contentArea.getScene().getWindow();
        File selectedFile = fc.showOpenDialog(window);
        if (selectedFile == null) return;

        String chosenCategory = promptForCategory(window);
        if (chosenCategory == null) chosenCategory = "Uncategorized";

        try {
            // Save into ~/.stackshelf/booksdata/
            File dir = new File(System.getProperty("user.home") + File.separator
                    + ".stackshelf" + File.separator + "booksdata");
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, selectedFile.getName());
            Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            String title = selectedFile.getName().replaceFirst("[.][^.]+$", "");

            FileTypeManager ftm = new FileTypeManager();
            ftm.fileType(dest.getAbsolutePath());
            int totalPages = ftm.getTotalPage();
            Image coverImage = ftm.getPage(0);
            ftm.close();

            // Save cover to disk (bro's approach - survives restarts)
            String coverPath = saveCover(coverImage, title);

            Book newBook = new Book(title, dest.getAbsolutePath(), totalPages,
                    chosenCategory, 0.0, coverPath, 0);

            BookDatabase.getInstance().addBook(newBook);
            changeToAllBooks(); // navigate back and refresh

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error adding book: " + e.getMessage()).showAndWait();
        }
    }

    /** Saves a JavaFX Image as PNG to covers/ folder, returns the path */
    public String saveCover(Image image, String title) {
        if (image == null) return null;
        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        File coversDir = new File(System.getProperty("user.home") + File.separator
                + ".stackshelf" + File.separator + "covers");
        coversDir.mkdirs();
        File file = new File(coversDir, safeTitle + ".png");
        try {
            java.awt.image.BufferedImage bImg = SwingFXUtils.fromFXImage(image, null);
            javax.imageio.ImageIO.write(bImg, "png", file);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Category picker dialog */
    private String promptForCategory(Window owner) {
        List<String> existing = BookDatabase.getInstance().getAllCategories();
        List<String> choices = new ArrayList<>();
        choices.add("Uncategorized");
        for (String c : existing) {
            if (!c.equals("Uncategorized") && !choices.contains(c)) choices.add(c);
        }
        choices.add("+ Create new category...");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Uncategorized", choices);
        dialog.setTitle("Choose Category");
        dialog.setHeaderText("Assign a category to this book");
        dialog.setContentText("Category:");
        if (owner != null) dialog.initOwner(owner);

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return null;
        String picked = result.get();

        if (picked.equals("+ Create new category...")) {
            TextInputDialog input = new TextInputDialog();
            input.setTitle("New Category");
            input.setHeaderText("Enter a name for the new category");
            input.setContentText("Category name:");
            if (owner != null) input.initOwner(owner);
            return input.showAndWait()
                    .map(String::trim).filter(s -> !s.isEmpty()).orElse("Uncategorized");
        }
        return picked;
    }

    // ── Book Tile (used by AllBooksController) ────────────────────────────────

    /** Creates a styled book card VBox for display in the grid */
    public VBox createBookTile(Book book) {
        VBox tile = new VBox(8);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.getStyleClass().add("book-card");
        tile.setPrefSize(160, 265);
        tile.setMaxSize(160, 265);

        // ── Cover image ──────────────────────────────────────────
        ImageView coverView = new ImageView();
        loadCoverIntoView(book, coverView);
        coverView.setFitWidth(138);
        coverView.setFitHeight(178);
        coverView.setPreserveRatio(true);
        // Placeholder bg when no cover yet
        coverView.setStyle("-fx-background-color: #1e2130;");

        // ── Favourite button (top-right, small pill) ─────────────
        Button favBtn = new Button(book.isFavourite() ? "♥" : "♡");
        styleFavBtn(favBtn, book.isFavourite());
        favBtn.setOnAction(e -> {
            e.consume();
            book.setFavouriteStatus(!book.isFavourite());
            BookDatabase.getInstance().updateBook(book);
            styleFavBtn(favBtn, book.isFavourite());
        });

        // ── Delete button (top-left, small circle) ───────────────
        Label deleteX = new Label("x");
        deleteX.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #e8ecf4;");
        StackPane deleteBtn = new StackPane(deleteX);
        deleteBtn.setPrefSize(20, 20);
        deleteBtn.setMinSize(20, 20);
        deleteBtn.getStyleClass().add("delete-button");
        deleteBtn.setOnMouseClicked(e -> {
            e.consume();
            deleteBook(book);
        });

        // ── Cover area: image + overlaid buttons ─────────────────
        AnchorPane coverArea = new AnchorPane(coverView, deleteBtn, favBtn);
        coverArea.setPrefSize(138, 178);
        coverArea.setMinSize(138, 178);
        coverArea.setMaxSize(138, 178);
        coverArea.setStyle("-fx-background-color: #1e2130; -fx-background-radius: 4;");
        AnchorPane.setTopAnchor(deleteBtn, 4.0);
        AnchorPane.setLeftAnchor(deleteBtn, 4.0);
        AnchorPane.setTopAnchor(favBtn, 4.0);
        AnchorPane.setRightAnchor(favBtn, 4.0);

        // ── Title ────────────────────────────────────────────────
        Label titleLbl = new Label(book.getTitle());
        titleLbl.getStyleClass().add("book-title");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(145);
        titleLbl.setAlignment(Pos.CENTER);

        // ── Thin progress bar ────────────────────────────────────
        ProgressBar progBar = new ProgressBar(book.getProgress());
        progBar.setPrefWidth(138);
        progBar.setPrefHeight(4);
        // Use CSS class — but also set accent inline as fallback
        progBar.setStyle("-fx-accent: #4f8ef7; -fx-pref-height: 4;");

        // ── Wire click to open (but not on overlay buttons) ──────
        tile.setOnMouseClicked(event -> {
            // Always use the current main controller instance, not the one
            // that created this tile (which may be stale after Back navigation)
            Controller current = Main.getMainController();
            if (current != null) current.openBook(book);
        });
        tile.getChildren().addAll(coverArea, titleLbl, progBar);
        return tile;
    }

    private void loadCoverIntoView(Book book, ImageView view) {
        // 1. Try disk-saved cover — load SYNCHRONOUSLY (requestedWidth=0 means full size)
        if (book.getCoverPath() != null) {
            File f = new File(book.getCoverPath());
            if (f.exists()) {
                // backgroundLoading=false forces synchronous load — no blank flicker
                Image img = new Image(f.toURI().toString(), 0, 0, true, true, false);
                if (!img.isError()) {
                    view.setImage(img);
                    return;
                }
                System.out.println("Cover file error for: " + book.getTitle());
            } else {
                System.out.println("Cover file missing: " + book.getCoverPath());
            }
        }

        // 2. Use cached in-memory image
        if (book.getCoverImage() != null) {
            view.setImage(book.getCoverImage());
            return;
        }

        // 3. Re-render from PDF and save for next time
        System.out.println("Regenerating cover for: " + book.getTitle());
        try {
            Image img = PDFEngine.renderingPage(book.getFilePath(), 0);
            if (img != null && !img.isError()) {
                book.setCoverImage(img);
                view.setImage(img);
                // Save to disk asynchronously so UI doesn't block
                javafx.application.Platform.runLater(() -> {
                    String savedPath = saveCover(img, book.getTitle());
                    if (savedPath != null) {
                        book.setCoverPath(savedPath);
                        BookDatabase.getInstance().updateBook(book);
                        System.out.println("Cover saved: " + savedPath);
                    }
                });
            }
        } catch (Exception e) {
            System.out.println("Could not generate cover for: " + book.getTitle() + " - " + e.getMessage());
        }
    }

    private void styleFavBtn(Button btn, boolean isFav) {
        if (isFav) {
            btn.setText("♥");
            btn.setStyle("-fx-background-color: rgba(79,142,247,0.85); -fx-text-fill: white; " +
                    "-fx-background-radius: 50; -fx-font-size: 11px; -fx-padding: 2 6; -fx-cursor: hand;");
        } else {
            btn.setText("♡");
            btn.setStyle("-fx-background-color: rgba(30,33,48,0.75); -fx-text-fill: #4f8ef7; " +
                    "-fx-background-radius: 50; -fx-font-size: 11px; -fx-padding: 2 6; " +
                    "-fx-border-color: rgba(79,142,247,0.5); -fx-border-width: 1; -fx-border-radius: 50; -fx-cursor: hand;");
        }
    }

    public void deleteBook(Book book) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete '" + book.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        if (alert.showAndWait().filter(b -> b == ButtonType.YES).isPresent()) {
            BookDatabase.getInstance().removeBook(book);
            if (currentAllBooksController != null) currentAllBooksController.loadBooks();
        }
    }

    public void openBook(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/bookreader/readerWindow.fxml"));
            Parent readerPage = loader.load();
            BookController readerBrain = loader.getController();

            // Swap the root of the EXISTING scene — no new Scene created,
            // so the window keeps its current size perfectly
            javafx.stage.Stage stage = Main.getPrimaryStage();
            javafx.scene.Scene scene = stage.getScene();
            scene.setRoot(readerPage);

            // startSession AFTER scene is live so pdfView is laid out
            javafx.application.Platform.runLater(() -> readerBrain.startSession(book));

        } catch (Exception e) {
            System.err.println("Failed to open reader: " + e.getMessage());
            e.printStackTrace();
        }
    }
}