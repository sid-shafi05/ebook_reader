package org.example.bookreader;

import javafx.scene.image.Image;

public class Book {
    private String title;
    private String pathFile;
    private String authorName;
    private int totalPages;
    private int lastReadPageNumber;
    private boolean isFavourite;
    private Image coverImage;  // ← ADD THIS

    public Book() {}

    // Updated constructor with coverImage
    public Book(String title, String path, Image coverImage, int total) {
        this.title = title;
        this.pathFile = path;
        this.coverImage = coverImage;  // ← ADD THIS
        this.totalPages = total;
        this.lastReadPageNumber = 0;
        this.isFavourite = false;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getFilePath() {
        return pathFile;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getLastReadPageNumber() {
        return lastReadPageNumber;
    }

    public boolean isFavourite() {  // Fixed method name
        return isFavourite;
    }

    public Image getCoverImage() {  // ← ADD THIS
        return coverImage;
    }

    public String getAuthorName() {
        return authorName;
    }

    // Setters
    public void setTitle(String title) {  // Fixed bug: was setting title=title
        this.title = title;
    }

    public void setFilePath(String path) {
        this.pathFile = path;
    }

    public void setTotalPages(int total) {
        this.totalPages = total;
    }

    public void setLastReadPageNumber(int last) {
        this.lastReadPageNumber = last;
    }

    public void setFavouriteStatus(boolean fav) {
        this.isFavourite = fav;
    }

    public void setCoverImage(Image coverImage) {  // ← ADD THIS
        this.coverImage = coverImage;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}