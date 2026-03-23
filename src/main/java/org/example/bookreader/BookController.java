package org.example.bookreader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.effect.ColorAdjust;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class BookController {
    private long sessionStartTime;
    private int sessionStartPage;
    private Book currentBook;
    private int currentPage;
    private int highestPageReached;
    private boolean focusModeActive = false; // track focus mode state
    private Timeline readingTimer; // for reading time display
    private int readingSeconds = 0; // accumulated reading time
    private ContextMenu currentMenu; // track open menu for toggle

    @FXML private ScrollPane pageScrollPane;
    @FXML private ImageView pdfView;
    @FXML private Label pageNumberLabel;
    @FXML private Label bookTitleLabel;
    @FXML private Button bookmarkButton;
    @FXML private TextField pageJumpField;
    @FXML private Slider pageSlider;
    @FXML private Label sliderMinLabel;
    @FXML private Label sliderMaxLabel;
    @FXML private Label sliderCurrentPageLabel;
    @FXML private Button focusModeButton;
    @FXML private Button menuButton;
    @FXML private Button closeNotebookButton;
    @FXML private HBox sliderSection;
    @FXML private HBox controlsSection;
    @FXML private VBox notebookPanel;
    @FXML private TextArea notesTextArea;

    private boolean sliderDragging = false; // prevent feedback loops
    private ColorAdjust colorAdjust = new ColorAdjust(); // for color filters
    private boolean notebookPanelVisible = false; // track notebook panel state

    // FileTypeManager handles both PDF and CBZ rendering, so we can use it for both types of books without needing separate engines in this controller
    private FileTypeManager fileTypeManager;

    public void startSession(Book book){
        this.currentBook = book;
        this.currentPage = book.getLastReadPageNumber();
        this.sessionStartTime = System.currentTimeMillis();
        this.sessionStartPage = currentPage;
        this.highestPageReached = currentPage;
        try{
            fileTypeManager = new FileTypeManager();
            fileTypeManager.fileType(book.getFilePath());

            // initialize page slider
            int totalPages = fileTypeManager.getTotalPage();
            pageSlider.setMin(1);
            pageSlider.setMax(totalPages);
            pageSlider.setValue(currentPage + 1); // convert 0-indexed to 1-indexed for display
            sliderMinLabel.setText("1");
            sliderMaxLabel.setText(String.valueOf(totalPages));

            // set initial label text
            if (sliderCurrentPageLabel != null) {
                sliderCurrentPageLabel.setText("Page " + (currentPage + 1));
            }

            // update current page label when slider value changes
            pageSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int pageNum = newVal.intValue();
                if (sliderCurrentPageLabel != null) {
                    sliderCurrentPageLabel.setText("Page " + pageNum);
                }
            });

            // handle slider dragging
            pageSlider.setOnMousePressed(event -> sliderDragging = true);
            pageSlider.setOnMouseReleased(event -> {
                sliderDragging = false;
                int newPage = (int) pageSlider.getValue() - 1; // convert back to 0-indexed
                if (newPage != currentPage && newPage >= 0 && newPage < totalPages) {
                    currentPage = newPage;
                    if (currentPage > highestPageReached) {
                        highestPageReached = currentPage;
                    }
                    renderCurrentPage();
                }
            });

            pdfView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if(newScene != null){
                    pdfView.fitWidthProperty().bind(newScene.widthProperty().multiply(0.95));
                }
            });
            if(pdfView.getScene() != null){
                pdfView.fitWidthProperty().bind(pdfView.getScene().widthProperty().multiply(0.95));
            }
            renderCurrentPage();
            startReadingTimer(); // start reading time tracking
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    private void renderCurrentPage(){
        if(fileTypeManager != null){
            pdfView.setImage(null);
            System.gc();
            pdfView.setImage(fileTypeManager.getPage(currentPage));
            if(pageNumberLabel != null){
                pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + fileTypeManager.getTotalPage());
            }
            if(bookTitleLabel != null && currentBook != null){
                bookTitleLabel.setText(currentBook.getTitle());
            }

            // sync slider with current page (only if not dragging to prevent conflicts)
            if (pageSlider != null && !sliderDragging) {
                pageSlider.setValue(currentPage + 1);
            }

            updateBookmarkButtonStyle();
            scrollToTop();
        }
    }

    private void scrollToTop(){
        if(pageScrollPane != null){
            javafx.application.Platform.runLater(() -> pageScrollPane.setVvalue(0));
        }
    }

    @FXML
    public void nextButtonLogic(){
        if(currentPage < fileTypeManager.getTotalPage() - 1){
            currentPage++;
            if(currentPage > highestPageReached){
                highestPageReached = currentPage;
            }
            renderCurrentPage();
        }
    }

    @FXML
    public void prevButtonLogic(){
        if(currentPage > 0){
            currentPage--;
            renderCurrentPage();
        }
    }

    @FXML
    public void onJumpToPageClick() {
        if (pageJumpField == null || fileTypeManager == null) return;

        String input = pageJumpField.getText().trim();

        // validate input is not empty
        if (input.isEmpty()) {
            showPageJumpError("Enter a page number");
            return;
        }

        try {
            // parse input as page number
            int pageNum = Integer.parseInt(input);
            int totalPages = fileTypeManager.getTotalPage();

            // validate page is in valid range (1-indexed for user, 0-indexed for code)
            if (pageNum < 1 || pageNum > totalPages) {
                showPageJumpError("Page must be 1-" + totalPages);
                return;
            }

            // SUCCESS: jump to page
            currentPage = pageNum - 1;

            // only update highestPageReached if jumping forward
            // this ensures progress bar shows furthest page reached, not current page
            if (currentPage > highestPageReached) {
                highestPageReached = currentPage;
            }

            renderCurrentPage();
            clearPageJumpField();

        } catch (NumberFormatException e) {
            // invalid number format
            showPageJumpError("Invalid number");
        }
    }

    private void showPageJumpError(String message) {
        if (pageJumpField == null) return;

        // red border
        pageJumpField.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2; -fx-padding: 6;");

        // show tooltip with error message
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(message);
        tooltip.setStyle("-fx-font-size: 10; -fx-text-fill: white; -fx-background-color: #cc0000;");
        pageJumpField.setTooltip(tooltip);

        // auto-hide tooltip after 3 seconds
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(3),
                event -> pageJumpField.setTooltip(null)
            )
        );
        timeline.play();
    }

    private void clearPageJumpField() {
        if (pageJumpField == null) return;

        // remove red border and tooltip
        pageJumpField.setStyle("-fx-padding: 8;");
        pageJumpField.setTooltip(null);
        pageJumpField.clear();
    }

    @FXML
    public void onBackButtonClick() {
        stopSession();
        javafx.stage.Stage stage = (javafx.stage.Stage) pdfView.getScene().getWindow();
        stage.close();
    }

    // set bookmark on the current page.

    @FXML
    public void toggleBookmark() {
        if (currentBook == null) return;

        if (BookmarkManager.isBookmarked(currentBook.getFilePath(), currentPage)) {
            BookmarkManager.removeBookmark(currentBook.getFilePath(), currentPage);
        } else {
            BookmarkManager.addBookmark(currentBook.getFilePath(), currentPage);
        }
        updateBookmarkButtonStyle();
    }

    /**
     * Update bookmark button style based on whether current page is bookmarked.
     */
    private void updateBookmarkButtonStyle() {
        if (bookmarkButton == null || currentBook == null) return;

        if (BookmarkManager.isBookmarked(currentBook.getFilePath(), currentPage)) {
            // Page is bookmarked - show filled bookmark with red color
            bookmarkButton.setText("🔖");
            bookmarkButton.setStyle("-fx-font-size: 20; -fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        } else {
            // Page not bookmarked - use default CSS class
            bookmarkButton.setText("🔖");
            bookmarkButton.setStyle("");
            bookmarkButton.getStyleClass().clear();
            bookmarkButton.getStyleClass().add("bookmark-button");
        }
    }

    /**
     * Show a panel/dialog with all bookmarks for current book.
     */
    @FXML
    public void showBookmarksPanel() {
        if (currentBook == null) return;

        List<Bookmark> bookmarks = BookmarkManager.getBookmarksForBook(currentBook.getFilePath());

        // Use BookmarkManager's modern dialog method
        int selectedPage = BookmarkManager.showBookmarkDialog(currentBook.getTitle(), bookmarks);

        if (selectedPage >= 0) {
            currentPage = selectedPage;

            // only update highestPageReached if jumping forward
            if (currentPage > highestPageReached) {
                highestPageReached = currentPage;
            }

            renderCurrentPage();
        }
    }

    // Check if current page is bookmarked.

    public boolean isCurrentPageBookmarked() {
        if (currentBook == null) return false;
        return BookmarkManager.isBookmarked(currentBook.getFilePath(), currentPage);
    }

    //Get all bookmarks for the current book.

    public List<Bookmark> getCurrentBookBookmarks() {
        if (currentBook == null) return new ArrayList<>();
        return BookmarkManager.getBookmarksForBook(currentBook.getFilePath());
    }

    //when user closes the book or goes back to the library
    public void stopSession(){
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - sessionStartTime) / 1000;
        int pagesReadThisSession = highestPageReached - sessionStartPage;

        int totalPages = fileTypeManager.getTotalPage();
        double newProgress;
        if(highestPageReached >= totalPages - 1){
            newProgress = 1.0;
        } else {
            newProgress = (double) highestPageReached / (totalPages - 1);
        }

        SingleReadingEvent event = new SingleReadingEvent(
            java.time.LocalDate.now().toString(),
            currentBook.getTitle(),
            pagesReadThisSession,
            seconds,
            currentBook.getCategory()
        );
        StatsManagement.saveNewEvent(event);

        currentBook.setLastReadPageNumber(currentPage);
        currentBook.setProgressValue(newProgress);

        List<Book> library = Library.loadBooks();
        for(Book b : library){
            if(b.getFilePath().equals(currentBook.getFilePath())){
                b.setLastReadPageNumber(currentPage);
                b.setProgressValue(newProgress);
            }
        }
        Library.saveBookList(library);

        // close the engine if it's a PDF
        stopReadingTimer(); // stop reading timer
        fileTypeManager.close();
        pdfView.fitWidthProperty().unbind();
    }

    // FOCUS MODE - toggle between full UI and minimal UI for distraction-free reading
    @FXML
    public void toggleFocusMode() {
        focusModeActive = !focusModeActive;

        if (focusModeActive) {
            // enter focus mode - hide slider and bookmark/jump controls
            if (sliderSection != null) sliderSection.setVisible(false);
            if (controlsSection != null) controlsSection.setVisible(false);
            if (focusModeButton != null) focusModeButton.setStyle("-fx-background-color: #4f9eff; -fx-text-fill: white;");
        } else {
            // exit focus mode - show slider and bookmark/jump controls
            if (sliderSection != null) sliderSection.setVisible(true);
            if (controlsSection != null) controlsSection.setVisible(true);
            if (focusModeButton != null) focusModeButton.setStyle("");
        }
    }

    // COLOR FILTERS
    @FXML
    public void setColorFilterNormal() {
        colorAdjust.setHue(0);
        colorAdjust.setBrightness(0);
        colorAdjust.setContrast(0);
        colorAdjust.setSaturation(0);
        pdfView.setEffect(null);
    }

    @FXML
    public void setColorFilterDark() {
        colorAdjust.setHue(0);
        colorAdjust.setBrightness(-0.2);
        colorAdjust.setContrast(0.2);
        colorAdjust.setSaturation(0);
        pdfView.setEffect(colorAdjust);
    }

    @FXML
    public void setColorFilterSepia() {
        colorAdjust.setHue(-0.1);
        colorAdjust.setBrightness(0.1);
        colorAdjust.setContrast(0);
        colorAdjust.setSaturation(-0.5);
        pdfView.setEffect(colorAdjust);
    }

    @FXML
    public void setColorFilterNight() {
        colorAdjust.setHue(0.3);
        colorAdjust.setBrightness(-0.4);
        colorAdjust.setContrast(0.3);
        colorAdjust.setSaturation(-0.7);
        pdfView.setEffect(colorAdjust);
    }

    // Show color filter menu with notebook option - toggles on/off
    @FXML
    public void showColorFilterMenu() {
        // If menu is already open, close it
        if (currentMenu != null && currentMenu.isShowing()) {
            currentMenu.hide();
            currentMenu = null;
            return;
        }

        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0; -fx-background-color: #2b2b2b; -fx-border-color: #3a3a3a;");

        // Color Modes Submenu
        MenuItem colorModesItem = new MenuItem("🎨 Color Modes");
        colorModesItem.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");

        ContextMenu colorMenu = new ContextMenu();
        colorMenu.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0; -fx-background-color: #2b2b2b; -fx-border-color: #3a3a3a;");

        MenuItem normalMode = new MenuItem("Normal Mode");
        normalMode.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");
        normalMode.setOnAction(e -> setColorFilterNormal());

        MenuItem darkMode = new MenuItem("Dark Mode");
        darkMode.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");
        darkMode.setOnAction(e -> setColorFilterDark());

        MenuItem sepiaFilter = new MenuItem("Sepia Filter");
        sepiaFilter.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");
        sepiaFilter.setOnAction(e -> setColorFilterSepia());

        MenuItem nightMode = new MenuItem("Night Mode");
        nightMode.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");
        nightMode.setOnAction(e -> setColorFilterNight());

        colorMenu.getItems().addAll(normalMode, darkMode, sepiaFilter, nightMode);
        colorModesItem.setOnAction(e -> {
            // Show submenu at cursor position
            Bounds bounds = menuButton.localToScreen(menuButton.getBoundsInLocal());
            colorMenu.show(menuButton.getScene().getWindow(), bounds.getCenterX() + 100, bounds.getCenterY());
        });

        // Separator
        SeparatorMenuItem sep = new SeparatorMenuItem();

        // Notebook option
        MenuItem notebookItem = new MenuItem("📝 Notebook");
        notebookItem.setStyle("-fx-font-size: 13px; -fx-text-fill: #e0e0e0;");
        notebookItem.setOnAction(e -> {
            menu.hide(); // Close the menu first
            currentMenu = null;
            toggleNotebookPanel();
        });

        menu.getItems().addAll(colorModesItem, sep, notebookItem);

        // Show menu at the position of the button
        if (menuButton != null) {
            Bounds bounds = menuButton.localToScreen(menuButton.getBoundsInLocal());
            menu.show(menuButton, bounds.getCenterX(), bounds.getCenterY() + 20);
            currentMenu = menu;
        }
    }

    // Toggle notebook panel visibility with slide animation
    @FXML
    public void toggleNotebookPanel() {
        if (notebookPanel == null) return;

        notebookPanelVisible = !notebookPanelVisible;

        if (notebookPanelVisible) {
            // Load notes when opening
            loadNotesFromFile();
            notebookPanel.setVisible(true);
            notebookPanel.setManaged(true);
        } else {
            notebookPanel.setVisible(false);
            notebookPanel.setManaged(false);
        }
    }

    // Load notes for current book from file
    private void loadNotesFromFile() {
        if (currentBook == null || notesTextArea == null) return;

        String notesFileName = "notes/" + currentBook.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_notes.txt";
        try {
            if (java.nio.file.Files.exists(java.nio.file.Paths.get(notesFileName))) {
                String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(notesFileName)));
                notesTextArea.setText(content);
            } else {
                notesTextArea.setText("");
            }
        } catch (IOException e) {
            System.out.println("Error loading notes: " + e.getMessage());
        }
    }

    // Save notes for current book to file
    @FXML
    public void saveNotesPanel() {
        if (currentBook == null || notesTextArea == null) return;

        String notes = notesTextArea.getText();
        String notesFileName = "notes/" + currentBook.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_notes.txt";

        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get("notes"));
            java.nio.file.Files.write(java.nio.file.Paths.get(notesFileName), notes.getBytes());
            System.out.println("Notes saved for: " + currentBook.getTitle());
        } catch (IOException e) {
            System.out.println("Error saving notes: " + e.getMessage());
        }
    }

    // READING TIME TRACKING
    private void startReadingTimer() {
        readingSeconds = 0;
        readingTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            readingSeconds++;
            updateReadingTimeDisplay();
        }));
        readingTimer.setCycleCount(Timeline.INDEFINITE);
        readingTimer.play();
    }

    private void updateReadingTimeDisplay() {
        // Reading time tracker removed per user request
    }

    private void stopReadingTimer() {
        if (readingTimer != null) {
            readingTimer.stop();
        }
    }
}


