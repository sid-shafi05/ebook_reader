package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class BookController {

    @FXML private ImageView pdfView;
    @FXML private Label pageNumberLabel;
    @FXML public HBox navigationBar;

    private Book currentBook;
    private PDFEngine engine;
    private int currentPage;
    private int pageProgress;
    private long sessionStartTime;
    private int sessionStartPage;

    /** Called by Controller.openBook() to load and start a reading session */
    public void startSession(Book book) {
        System.out.println("BookController.startSession: " + book.getTitle());
        this.currentBook = book;
        this.currentPage = book.getLastReadPageNumber();
        this.sessionStartPage = currentPage;
        this.pageProgress = currentPage;
        this.sessionStartTime = System.currentTimeMillis();

        try {
            this.engine = new PDFEngine(book.getFilePath());
            renderCurrentPage();
        } catch (IOException e) {
            System.err.println("Error opening PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderCurrentPage() {
        if (engine != null && pdfView != null) {
            pdfView.setImage(engine.renderingPage(currentPage));
            if (pageNumberLabel != null) {
                pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + engine.getPageCount());
            }
        }
    }

    @FXML
    public void nextButtonLogic() {
        if (engine != null && currentPage < engine.getPageCount() - 1) {
            currentPage++;
            pageProgress++;
            renderCurrentPage();
        }
    }

    @FXML
    public void prevButtonLogic() {
        if (currentPage > 0) {
            currentPage--;
            renderCurrentPage();
        }
    }

    @FXML
    public void onBackButtonClick() {
        stopSession();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/org/example/bookreader/mainscreen.fxml"));
            javafx.scene.Parent root = loader.load();

            // Register the new Controller BEFORE swapping root so that
            // initialize() -> loadBooks() -> tile clicks all see the right instance
            Controller newCtrl = loader.getController();
            Main.setMainController(newCtrl);

            // Swap root on existing scene — window keeps its size perfectly
            Main.getPrimaryStage().getScene().setRoot(root);

        } catch (Exception e) {
            System.err.println("Error returning to library: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Saves progress and logs reading stats */
    public void stopSession() {
        if (currentBook == null) return;

        long seconds = (System.currentTimeMillis() - sessionStartTime) / 1000;
        int pagesRead = Math.abs(pageProgress - sessionStartPage);

        // Update progress value (0.0 to 1.0)
        if (currentBook.getTotalPages() > 0) {
            currentBook.setProgressValue((double) currentPage / currentBook.getTotalPages());
        }
        currentBook.setLastReadPageNumber(currentPage);

        // Persist to database
        BookDatabase.getInstance().updateBook(currentBook);

        // Log reading event for stats
        StatsManagement.readLog(
                currentBook.getTitle(), pagesRead, seconds, currentBook.getCategory());

        System.out.println("Session ended: " + pagesRead + " pages, " + seconds + "s");

        if (engine != null) {
            engine.close();
            engine = null;
        }
    }
}