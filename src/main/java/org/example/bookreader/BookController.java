package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class BookController {
    @FXML
    private ImageView pdfView;

    @FXML
    private Label pageNumberLabel;

    private Book currentBook;
    private FileTypeManager fileTypeManager;
    private int currentPage = 0;
    private long sessionStartTime;
    private int sessionStartPage;

    public void setBook(Book book) throws IOException {
        System.out.println("BookController.setBook called for: " + book.getTitle());
        this.currentBook = book;
        this.fileTypeManager = new FileTypeManager();
        this.fileTypeManager.fileType(book.getFilePath());

        // Resume from last read page
        this.currentPage = book.getLastReadPageNumber();
        this.sessionStartPage = currentPage;
        this.sessionStartTime = System.currentTimeMillis();

        displayCurrentPage();
        System.out.println("Book loaded! Starting at page: " + (currentPage + 1));
    }

    private void displayCurrentPage() {
        if (fileTypeManager != null) {
            pdfView.setImage(fileTypeManager.getPage(currentPage));
            pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + fileTypeManager.getTotalPage());
        }
    }

    @FXML
    public void nextButtonLogic() {
        if (currentPage < fileTypeManager.getTotalPage() - 1) {
            currentPage++;
            displayCurrentPage();
            saveProgress();
        }
    }

    @FXML
    public void prevButtonLogic() {
        if (currentPage > 0) {
            currentPage--;
            displayCurrentPage();
            saveProgress();
        }
    }

    private void saveProgress() {
        if (currentBook != null) {
            currentBook.setLastReadPageNumber(currentPage);
            BookDatabase.getInstance().updateBook(currentBook);
            System.out.println("Progress saved: Page " + (currentPage + 1));
        }
    }

    @FXML
    public void onBackButtonClick() {
        try {
            System.out.println("Going back to library...");

            // Save final progress
            saveProgress();

            // Calculate and log session stats
            long sessionEndTime = System.currentTimeMillis();
            long secondsRead = (sessionEndTime - sessionStartTime) / 1000;
            int pagesRead = Math.abs(currentPage - sessionStartPage);

            System.out.println("Session stats:");
            System.out.println("- Pages read: " + pagesRead);
            System.out.println("- Time: " + secondsRead + " seconds");

            // Log to stats (your friend's code)
            StatsManagement.readLog(
                    currentBook.getTitle(),
                    pagesRead,
                    secondsRead,
                    currentBook.getCategory()
            );

            // Close file manager
            if (fileTypeManager != null) {
                fileTypeManager.close();
            }

            // Return to main screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("mainscreen.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) pdfView.getScene().getWindow();
            Scene scene = new Scene(root, 986, 616);

            try {
                String css = getClass().getResource("application.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("Warning: Could not load CSS");
            }

            stage.setScene(scene);
            System.out.println("Returned to library");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}