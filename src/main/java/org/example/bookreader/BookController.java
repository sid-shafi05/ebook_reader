package org.example.bookreader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class BookController {
    private long sessionStartTime;
    private int sessionStartPage;
    private Book currentBook;
    private int currentPage;
    private int highestPageReached;

    @FXML private ScrollPane pageScrollPane;
    @FXML private ImageView pdfView;
    @FXML private Label pageNumberLabel;
    @FXML private Label bookTitleLabel;
    @FXML private Button bookmarkButton;

    // FileTypeManager handles both PDF and CBZ transparently
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

            pdfView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if(newScene != null){
                    pdfView.fitWidthProperty().bind(newScene.widthProperty().multiply(0.95));
                }
            });
            if(pdfView.getScene() != null){
                pdfView.fitWidthProperty().bind(pdfView.getScene().widthProperty().multiply(0.95));
            }
            renderCurrentPage();
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
        fileTypeManager.close();
        pdfView.fitWidthProperty().unbind();
    }
}
