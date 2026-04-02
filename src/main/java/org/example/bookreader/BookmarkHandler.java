package org.example.bookreader;

import javafx.scene.control.Button;
import java.util.List;

public class BookmarkHandler {
    private Button bookmarkButton;
    private Book currentBook;
    private PageNavigator navigator;

    public BookmarkHandler(Button bookmarkButton, PageNavigator navigator) {
        this.bookmarkButton = bookmarkButton;
        this.navigator = navigator;
    }

    public void setCurrentBook(Book book) {
        this.currentBook = book;
    }

    public void toggleBookmark() {
        if (currentBook == null) return;
        int page = navigator.getCurrentPage();
        if (BookmarkManager.isBookmarked(currentBook.getFilePath(), page)) {
            BookmarkManager.removeBookmark(currentBook.getFilePath(), page);
        } else {
            BookmarkManager.addBookmark(currentBook.getFilePath(), page);
        }
        updateBookmarkButtonStyle();
    }

    public void updateBookmarkButtonStyle() {
        if (bookmarkButton == null || currentBook == null) return;
        int page = navigator.getCurrentPage();
        if (BookmarkManager.isBookmarked(currentBook.getFilePath(), page)) {
            bookmarkButton.setText("❐");
            bookmarkButton.setStyle(
                    "-fx-font-size: 20; -fx-background-color: #FF5722;" +
                            "-fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"
            );
        } else {
            bookmarkButton.setText("❐");
            bookmarkButton.setStyle("");
            bookmarkButton.getStyleClass().clear();
            bookmarkButton.getStyleClass().add("bookmark-button");
        }
    }

    public void showBookmarksPanel() {
        if (currentBook == null) return;
        List<Bookmark> bookmarks = BookmarkManager.getBookmarksForBook(currentBook.getFilePath());
        int selectedPage = BookmarkManager.showBookmarkDialog(currentBook.getTitle(), bookmarks);
        if (selectedPage >= 0) {
            navigator.setCurrentPage(selectedPage);
        }
    }
}