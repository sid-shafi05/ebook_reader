package org.example.bookreader;

import javafx.scene.image.Image;

public class Book {
    private String title;
    private String pathFile;
    private String authorName;
    private String category;
    private int totalPages;
    private int lastReadPageNumber;
    private boolean isFavourite;
    private Image coverImage;

    public Book() {}

    // Constructor for adding books from file chooser
    public Book(String title, String path) {
        this.title = title;
        this.pathFile = path;
        this.authorName = "Unknown Author";
        this.category = "Uncategorized";
        this.totalPages = 0;
        this.lastReadPageNumber = 0;
        this.isFavourite = false;
        this.coverImage = null;
    }

    // Full constructor
    public Book(String title, String path, Image coverImage, int total) {
        this.title = title;
        this.pathFile = path;
        this.coverImage = coverImage;
        this.totalPages = total;
        this.authorName = "Unknown Author";
        this.category = "Uncategorized";
        this.lastReadPageNumber = 0;
        this.isFavourite = false;
    }

    // Getters
    public String getTitle() { return title; }
    public String getFilePath() { return pathFile; }
    public int getTotalPages() { return totalPages; }
    public int getLastReadPageNumber() { return lastReadPageNumber; }
    public boolean isFavourite() { return isFavourite; }
    public Image getCoverImage() { return coverImage; }
    public String getAuthorName() { return authorName; }
    public String getCategory() { return category; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setFilePath(String path) { this.pathFile = path; }
    public void setTotalPages(int total) { this.totalPages = total; }
    public void setLastReadPageNumber(int last) { this.lastReadPageNumber = last; }
    public void setFavouriteStatus(boolean fav) { this.isFavourite = fav; }
    public void setCoverImage(Image coverImage) { this.coverImage = coverImage; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setCategory(String category) { this.category = category; }
}