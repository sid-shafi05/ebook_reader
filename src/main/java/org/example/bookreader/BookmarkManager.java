package org.example.bookreader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BookmarkManager {
    private static final String BOOKMARKS_FILE = "bookmarks.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void addBookmark(String bookPath, int pageNumber) {
        addBookmark(new Bookmark(bookPath, pageNumber));
    }

    public static void addBookmark(Bookmark bookmark) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();
            bookmarks.removeIf(b -> b.getBookPath().equals(bookmark.getBookPath())
                    && b.getPageNumber() == bookmark.getPageNumber());
            bookmarks.add(bookmark);
            saveBookmarks(bookmarks);
        } catch (Exception e) {
            System.err.println("Error adding bookmark: " + e.getMessage());
        }
    }

    public static void removeBookmark(String bookPath, int pageNumber) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();
            bookmarks.removeIf(b -> b.getBookPath().equals(bookPath)
                    && b.getPageNumber() == pageNumber);
            saveBookmarks(bookmarks);
        } catch (Exception e) {
            System.err.println("Error removing bookmark: " + e.getMessage());
        }
    }

    public static List<Bookmark> getBookmarksForBook(String bookPath) {
        try {
            return loadBookmarks().stream()
                    .filter(b -> b.getBookPath().equals(bookPath))
                    .sorted((b1, b2) -> Integer.compare(b1.getPageNumber(), b2.getPageNumber()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting bookmarks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static boolean isBookmarked(String bookPath, int pageNumber) {
        return getBookmarksForBook(bookPath).stream()
                .anyMatch(b -> b.getPageNumber() == pageNumber);
    }

    public static Bookmark getBookmark(String bookPath, int pageNumber) {
        return getBookmarksForBook(bookPath).stream()
                .filter(b -> b.getPageNumber() == pageNumber)
                .findFirst().orElse(null);
    }

    private static void saveBookmarks(List<Bookmark> bookmarks) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(BOOKMARKS_FILE), bookmarks);
        } catch (Exception e) {
            System.err.println("Error saving bookmarks: " + e.getMessage());
        }
    }

    public static List<Bookmark> loadBookmarks() {
        File file = new File(BOOKMARKS_FILE);
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file, new TypeReference<List<Bookmark>>() {});
        } catch (Exception e) {
            System.err.println("Error loading bookmarks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void clearBookmarksForBook(String bookPath) {
        try {
            List<Bookmark> bookmarks = loadBookmarks();
            bookmarks.removeIf(b -> b.getBookPath().equals(bookPath));
            saveBookmarks(bookmarks);
        } catch (Exception e) {
            System.err.println("Error clearing bookmarks: " + e.getMessage());
        }
    }

    /**
     * Show the bookmark dialog. Returns the selected page number, or -1 if none.
     * All visual styling is done via CSS classes — no inline setStyle() calls.
     */
    public static int showBookmarkDialog(String bookTitle, List<Bookmark> bookmarks) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.setTitle("Bookmarks");

        final int[] selectedPage = {-1};

        // ── Root container ───────────────────────────────────────────────
        VBox root = new VBox(0);
        root.getStyleClass().add("bookmark-dialog-root");
        root.setPrefWidth(450);
        root.setMaxWidth(450);

        // ── Header ───────────────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("bookmark-dialog-header");
        header.setPadding(new Insets(22, 20, 22, 20));

        Label titleLabel = new Label("🔖 Bookmarks");
        titleLabel.getStyleClass().add("bookmark-dialog-title");
        header.getChildren().add(titleLabel);

        // ── Subtitle ──────────────────────────────────────────────────────
        HBox subtitleBox = new HBox();
        subtitleBox.setAlignment(Pos.CENTER);
        subtitleBox.setPadding(new Insets(14, 20, 10, 20));

        Label bookLabel = new Label(bookTitle);
        bookLabel.getStyleClass().add("bookmark-dialog-subtitle");
        subtitleBox.getChildren().add(bookLabel);

        // ── Content ───────────────────────────────────────────────────────
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 25, 25, 25));
        content.setAlignment(Pos.TOP_CENTER);

        if (bookmarks.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(30, 20, 30, 20));
            emptyBox.getStyleClass().add("bookmark-empty-box");

            Label emptyIcon = new Label("📑");
            emptyIcon.setStyle("-fx-font-size: 48px;");

            Label emptyLabel = new Label("No Bookmarks Yet");
            emptyLabel.getStyleClass().add("bookmark-empty-title");

            Label emptyHint = new Label("Click the 🔖 button while reading\nto bookmark a page!");
            emptyHint.getStyleClass().add("bookmark-empty-hint");

            emptyBox.getChildren().addAll(emptyIcon, emptyLabel, emptyHint);
            content.getChildren().add(emptyBox);
        } else {
            Label instructionLabel = new Label("Select a page to jump to:");
            instructionLabel.getStyleClass().add("bookmark-instruction");

            ListView<String> listView = new ListView<>();
            listView.getStyleClass().add("bookmark-list");
            listView.setPrefHeight(200);

            for (Bookmark bookmark : bookmarks)
                listView.getItems().add("📄 Page " + (bookmark.getPageNumber() + 1));

            listView.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    int index = listView.getSelectionModel().getSelectedIndex();
                    if (index >= 0) {
                        selectedPage[0] = bookmarks.get(index).getPageNumber();
                        dialog.close();
                    }
                }
            });

            content.getChildren().addAll(instructionLabel, listView);
        }

        // ── Buttons ───────────────────────────────────────────────────────
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(0, 25, 25, 25));

        if (!bookmarks.isEmpty()) {
            Button okButton = new Button("Jump to Page");
            okButton.getStyleClass().add("bookmark-ok-btn");
            okButton.setOnAction(e -> {
                ListView<String> lv = (ListView<String>) content.getChildren().get(1);
                int index = lv.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    selectedPage[0] = bookmarks.get(index).getPageNumber();
                    dialog.close();
                }
            });
            buttonBox.getChildren().add(okButton);
        }

        Button cancelButton = new Button(bookmarks.isEmpty() ? "Close" : "Cancel");
        cancelButton.getStyleClass().add("bookmark-cancel-btn");
        cancelButton.setOnAction(e -> dialog.close());
        buttonBox.getChildren().add(cancelButton);

        // ── Assemble ──────────────────────────────────────────────────────
        root.getChildren().addAll(header, subtitleBox, content, buttonBox);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        // Load the app stylesheet so CSS classes resolve
        String css = BookmarkManager.class.getResource(
                "/org/example/bookreader/application.css").toExternalForm();
        scene.getStylesheets().add(css);

        dialog.setScene(scene);
        dialog.showAndWait();
        return selectedPage[0];
    }
}