package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Controller {

    private static final String BASE_DATA_PATH =
            System.getProperty("user.home") + File.separator + "Desktop"
                    + File.separator + "ebook_project_data";

    @FXML private AnchorPane contentArea;
    @FXML private VBox topBar;
    @FXML private TextField searchField;
    @FXML private Button allBtn, favBtn, catBtn, statsBtn, addBtn, dailyTargetBtn;
    @FXML private Label titleLabel, progressLabel, dateLabel;

    private FlowPane bookGrid;
    private FlowPane favGrid;
    private Parent allbooksPage, favPage, catPage, statsPage;
    private StatsController statsCtrl;

    private LibraryManager libraryManager;
    private BookAdder bookAdder;
    private boolean suppressSearch = false;

    @FXML
    public void initialize() {
        new File(BASE_DATA_PATH + File.separator + "booksdata").mkdirs();
        new File(BASE_DATA_PATH + File.separator + "covers").mkdirs();

        List<Book> bookList = Library.loadBooks();

        try {
            FXMLLoader l1 = new FXMLLoader(getClass().getResource("allbooks.fxml"));
            allbooksPage = l1.load();
            bookGrid = (FlowPane) l1.getNamespace().get("bookGrid");
            anchorFill(allbooksPage);

            FXMLLoader l2 = new FXMLLoader(getClass().getResource("fav.fxml"));
            favPage = l2.load();
            favGrid = (FlowPane) l2.getNamespace().get("favGrid");
            anchorFill(favPage);

            FXMLLoader l3 = new FXMLLoader(getClass().getResource("cat.fxml"));
            catPage = l3.load();
            catPage.getProperties().put("controller", l3.getController());
            anchorFill(catPage);

            FXMLLoader l4 = new FXMLLoader(getClass().getResource("stats.fxml"));
            statsPage = l4.load();
            statsCtrl = l4.getController();
            anchorFill(statsPage);

            contentArea.getChildren().addAll(allbooksPage, favPage, catPage, statsPage);

        } catch (IOException e) {
            e.printStackTrace();
        }

        libraryManager = new LibraryManager(bookGrid, favGrid, bookList, this);
        bookAdder = new BookAdder(BASE_DATA_PATH, bookList, this::refreshBookGrid);

        showPage(allbooksPage, true);
        setActiveStyle(allBtn);
        libraryManager.fillGrid(bookGrid, false);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (suppressSearch) return;
                libraryManager.filterBookGrid(newVal.trim().toLowerCase());
            });
        }

        if (dailyTargetBtn != null) {
            int target = getDailyTarget();
            dailyTargetBtn.setText("Daily Target: " + target + " minutes");
        }
    }

    private void anchorFill(Parent page) {
        AnchorPane.setTopAnchor(page, 0.0);
        AnchorPane.setBottomAnchor(page, 0.0);
        AnchorPane.setLeftAnchor(page, 0.0);
        AnchorPane.setRightAnchor(page, 0.0);
    }

    private void showPage(Parent pageToShow, boolean showTopBar) {
        allbooksPage.setVisible(false); allbooksPage.setManaged(false);
        favPage.setVisible(false);      favPage.setManaged(false);
        catPage.setVisible(false);      catPage.setManaged(false);
        statsPage.setVisible(false);    statsPage.setManaged(false);
        pageToShow.setVisible(true);    pageToShow.setManaged(true);
        if (topBar != null) { topBar.setVisible(showTopBar); topBar.setManaged(showTopBar); }
    }

    @FXML public void changeToAllBooks() {
        showPage(allbooksPage, true);
        setActiveStyle(allBtn);
        clearSearch();
        libraryManager.fillGrid(bookGrid, false);
    }

    @FXML public void changetoFavourites() {
        showPage(favPage, false);
        setActiveStyle(favBtn);
        libraryManager.fillGrid(favGrid, true);
    }

    @FXML public void changeToCategories() {
        showPage(catPage, false);
        setActiveStyle(catBtn);
        CatController catCtrl = (CatController) catPage.getProperties().get("controller");
        if (catCtrl != null) catCtrl.loadCategories();
    }

    @FXML public void changeToStats() {
        if (statsCtrl != null) statsCtrl.onPageShown();
        showPage(statsPage, false);
        setActiveStyle(statsBtn);
    }

    @FXML public void onAddBookButtonClick() {
        bookAdder.addBook();
    }

    @FXML public void sortByTitle() {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ▼");
        dateLabel.setText("Date");
        progressLabel.setText("Progress");
        libraryManager.sortByTitle();
    }

    @FXML public void sortByDate() {
        setActiveSort(dateLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ▼");
        progressLabel.setText("Progress");
        libraryManager.sortByDate();
    }

    @FXML public void sortByProgress() {
        setActiveSort(progressLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date");
        progressLabel.setText("Progress ▼");
        libraryManager.sortByProgress();
    }

    public void refreshBookGrid() {
        libraryManager.refreshBookGrid();
    }

    private void clearSearch() {
        suppressSearch = true;
        if (searchField != null) searchField.clear();
        libraryManager.setCurrentQuery("");
        suppressSearch = false;
    }

    private void setActiveStyle(Button clicked) {
        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        statsBtn.getStyleClass().remove("active");
        clicked.getStyleClass().add("active");
    }

    private void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        active.getStyleClass().add("label-color-active");
    }

    public void deleteBook(Book bookToDelete) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete '" + bookToDelete.getTitle() + "'?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Book?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            libraryManager.getBookList().remove(bookToDelete);
            Library.saveBookList(libraryManager.getBookList());
            refreshBookGrid();
        }
    }

    public void loadReaderScreen(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/bookreader/readerWindow.fxml"));
            Parent readerPage = loader.load();
            BookController readerBrain = loader.getController();
            readerBrain.startSession(book);

            javafx.stage.Stage readerStage = new javafx.stage.Stage();
            javafx.scene.Scene scene = new javafx.scene.Scene(readerPage, 1000, 700);
            String css = getClass().getResource(
                    "/org/example/bookreader/application.css").toExternalForm();
            scene.getStylesheets().add(css);
            readerStage.setTitle(book.getTitle() + " - StackShelf Reader");
            readerStage.setScene(scene);
            readerStage.setMaximized(true);
            readerStage.setOnHidden(e -> refreshBookGrid());
            readerStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Called by LibraryManager/CatController to open a book
    public void openBook(Book book) {
        loadReaderScreen(book);
    }

    @FXML public void setDailyTarget() {
        int current = getDailyTarget();
        TextInputDialog dialog = new TextInputDialog(String.valueOf(current));
        dialog.setTitle("Daily Reading Target");
        dialog.setHeaderText("Set Your Daily Reading Goal");
        dialog.setContentText("Minutes per day:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(minutes -> {
            try {
                int target = Integer.parseInt(minutes.trim());
                if (target > 0) {
                    java.nio.file.Files.write(
                            java.nio.file.Paths.get("daily_target.txt"),
                            String.valueOf(target).getBytes());
                    if (dailyTargetBtn != null)
                        dailyTargetBtn.setText("Daily Target: " + target + " minutes");
                }
            } catch (Exception e) {
                System.out.println("Invalid target: " + e.getMessage());
            }
        });
    }

    public static int getDailyTarget() {
        try {
            if (java.nio.file.Files.exists(java.nio.file.Paths.get("daily_target.txt"))) {
                String content = new String(java.nio.file.Files.readAllBytes(
                        java.nio.file.Paths.get("daily_target.txt")));
                return Integer.parseInt(content.trim());
            }
        } catch (Exception e) {
            System.out.println("Error reading daily target: " + e.getMessage());
        }
        return 60;
    }

    // ---- Book tile creation (kept here since it touches UI + Controller state) ----
    VBox createBookTile(Book book) {
        VBox tile = new VBox(6);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.getStyleClass().add("book-card");
        tile.setStyle("-fx-padding: 10; -fx-background-color: #2d2d2d; -fx-background-radius: 8;");
        tile.setPrefSize(180, 270);

        ImageView coverView = new ImageView();
        coverView.setFitWidth(150); coverView.setFitHeight(130);
        coverView.setPreserveRatio(true); coverView.setSmooth(true);

        boolean coverLoaded = false;
        if (book.getCoverPath() != null) {
            File imageFile = new File(book.getCoverPath());
            if (imageFile.exists()) {
                coverView.setImage(new Image(imageFile.toURI().toString()));
                coverLoaded = true;
            }
        }

        StackPane coverBox = new StackPane();
        coverBox.setPrefWidth(158); coverBox.setPrefHeight(135);
        coverBox.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 5;");
        if (coverLoaded) {
            coverBox.getChildren().add(coverView);
            StackPane.setAlignment(coverView, javafx.geometry.Pos.CENTER);
        } else {
            Label placeholder = new Label("📄");
            placeholder.setStyle("-fx-font-size: 40;");
            coverBox.getChildren().add(placeholder);
            StackPane.setAlignment(placeholder, javafx.geometry.Pos.CENTER);
        }

        StackPane deleteButton = new StackPane();
        deleteButton.setPrefSize(20, 20);
        deleteButton.getStyleClass().add("delete-button");
        Label deleteText = new Label("✕");
        deleteText.setTextFill(javafx.scene.paint.Color.WHITE);
        deleteText.setStyle("-fx-font-size: 9pt; -fx-font-weight: bold;");
        deleteButton.getChildren().add(deleteText);
        deleteButton.setOnMouseClicked(event -> { deleteBook(book); event.consume(); });

        Button heartBtn = new Button(book.isFavourite() ? "❤" : "♡");
        String heartActiveStyle = "-fx-background-color: #e8174a; -fx-background-radius: 12;" +
                "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6;" +
                "-fx-text-fill: white; -fx-font-weight: bold;";
        String heartInactiveStyle = "-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 12;" +
                "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 2 6 2 6;" +
                "-fx-text-fill: #555555; -fx-font-weight: bold;";
        heartBtn.setStyle(book.isFavourite() ? heartActiveStyle : heartInactiveStyle);
        heartBtn.setOnMouseClicked(event -> {
            book.setFavouriteStatus(!book.isFavourite());
            heartBtn.setText(book.isFavourite() ? "❤" : "♡");
            heartBtn.setStyle(book.isFavourite() ? heartActiveStyle : heartInactiveStyle);
            List<Book> library = Library.loadBooks();
            for (Book b : library)
                if (b.getFilePath().equals(book.getFilePath()))
                    b.setFavouriteStatus(book.isFavourite());
            Library.saveBookList(library);
            event.consume();
        });

        AnchorPane coverWrapper = new AnchorPane();
        coverWrapper.setPrefWidth(158); coverWrapper.setPrefHeight(135);
        AnchorPane.setTopAnchor(coverBox, 0.0); AnchorPane.setLeftAnchor(coverBox, 0.0);
        AnchorPane.setRightAnchor(coverBox, 0.0); AnchorPane.setBottomAnchor(coverBox, 0.0);
        AnchorPane.setTopAnchor(deleteButton, 3.0); AnchorPane.setRightAnchor(deleteButton, 3.0);
        AnchorPane.setTopAnchor(heartBtn, 3.0); AnchorPane.setLeftAnchor(heartBtn, 3.0);
        coverWrapper.getChildren().addAll(coverBox, heartBtn, deleteButton);

        tile.setOnMouseClicked(event -> {
            if (!deleteButton.isHover() && !heartBtn.isHover()) openBook(book);
        });

        Label titleLbl = new Label(book.getTitle());
        titleLbl.setWrapText(true); titleLbl.setMaxWidth(155);
        titleLbl.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label dateAddedLbl = new Label();
        if (book.getDateAdded() > 0) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM d, yyyy");
            dateAddedLbl.setText("Added: " + sdf.format(new java.util.Date(book.getDateAdded())));
        }
        dateAddedLbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 9px;");

        Label pagesLbl = new Label(book.getTotalPages() + " Pages");
        pagesLbl.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 10px;");

        int pct = (int)(book.getProgressValue() * 100);
        Label progressLbl = new Label(pct + "% read");
        progressLbl.setStyle("-fx-text-fill: #39FF14; -fx-font-size: 9px; -fx-font-weight: bold;");

        ProgressBar progBar = new ProgressBar(book.getProgressValue());
        progBar.setPrefWidth(155); progBar.setPrefHeight(8);
        progBar.setStyle("-fx-accent: #39FF14; -fx-control-inner-background: #444444;" +
                "-fx-background-color: #444444; -fx-background-radius: 4; -fx-padding: 0;");

        tile.getChildren().addAll(coverWrapper, titleLbl, dateAddedLbl, pagesLbl, progressLbl, progBar);
        return tile;
    }
}