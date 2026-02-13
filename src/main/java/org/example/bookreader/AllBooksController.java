package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AllBooksController {
    @FXML
    private FlowPane bookGrid;

    @FXML
    public void initialize() {
        System.out.println("=== AllBooksController initialized ===");
        loadBooks();
    }

    public void loadBooks() {
        System.out.println("=== Loading books ===");

        if (bookGrid == null) {
            System.out.println("ERROR: bookGrid is NULL!");
            return;
        }

        bookGrid.getChildren().clear();

        int bookCount = BookDatabase.getInstance().getBookCount();
        System.out.println("Books in database: " + bookCount);

        if (bookCount == 0) {
            Label emptyLabel = new Label("No books yet.\nClick 'Add Book' to get started!");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-padding: 50;");
            bookGrid.getChildren().add(emptyLabel);
            return;
        }

        for (Book book : BookDatabase.getInstance().getAllBooks()) {
            System.out.println("Creating card for: " + book.getTitle());
            createBookCard(book);
        }

        System.out.println("Total items in grid: " + bookGrid.getChildren().size());
    }

    private void createBookCard(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("book.fxml"));

            VBox bookCard = loader.load();

            ImageView coverView = (ImageView) bookCard.lookup("#bookCoverView");
            Label titleLabel = (Label) bookCard.lookup("#bookTitleLabel");

            if (titleLabel != null) {
                titleLabel.setText(book.getTitle());
            }

            // Regenerate cover if not in memory
            if (coverView != null) {
                if (book.getCoverImage() == null) {
                    // Generate cover image on-demand
                    try {
                        Image coverImage = PDFEngine.renderingPage(book.getFilePath(), 0);
                        book.setCoverImage(coverImage);
                        coverView.setImage(coverImage);
                        System.out.println("Generated cover for: " + book.getTitle());
                    } catch (Exception e) {
                        System.out.println("Could not generate cover: " + e.getMessage());
                    }
                } else {
                    coverView.setImage(book.getCoverImage());
                }
            }

            bookCard.setOnMouseClicked(event -> {
                System.out.println("Opening book: " + book.getTitle());
                openBook(book);
            });

            bookCard.setStyle(bookCard.getStyle() + "-fx-cursor: hand;");
            bookGrid.getChildren().add(bookCard);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openBook(Book book) {
        try {
            System.out.println("=== Opening Book Reader ===");

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("bookreader.fxml"));

            if (loader.getLocation() == null) {
                System.out.println("ERROR: Cannot find bookreader.fxml!");
                return;
            }

            Parent root = loader.load();

            BookController bookController = loader.getController();
            bookController.setBook(book);

            Stage stage = (Stage) bookGrid.getScene().getWindow();
            Scene scene = new Scene(root);

            try {
                String css = getClass().getResource("application.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("Warning: Could not load CSS");
            }

            stage.setScene(scene);

        } catch (Exception e) {
            System.out.println("ERROR opening book reader:");
            e.printStackTrace();
        }
    }

    public void refreshBooks() {
        System.out.println("=== Refreshing books display ===");
        loadBooks();
    }
}