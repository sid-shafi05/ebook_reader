package org.example.bookreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.scene.image.Image;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    // Core fields
    private String title;
    private String pathFile;       // JSON key: filePath via getter/setter
    private String authorName;
    private String category;
    private int totalPages;
    private int lastReadPageNumber;
    private boolean isFavourite;

    // Cover: saved to disk as PNG (bro's approach - survives restarts)
    private String coverPath;

    // Progress as 0.0-1.0 value (bro's approach - used by ProgressBar)
    private double progressValue;

    // Date added for sorting (our feature)
    private long dateAdded;

    // Transient - not serialized, regenerated at runtime
    private transient Image coverImage;

    public Book() {}

    // Constructor used when adding a new book
    public Book(String title, String filePath, int totalPages, String category,
                double progressValue, String coverPath, int lastReadPageNumber) {
        this.title = title;
        this.pathFile = filePath;
        this.totalPages = totalPages;
        this.category = category;
        this.progressValue = progressValue;
        this.coverPath = coverPath;
        this.lastReadPageNumber = lastReadPageNumber;
        this.authorName = "Unknown Author";
        this.isFavourite = false;
        this.dateAdded = System.currentTimeMillis();
    }

    // Minimal constructor (used by BookDatabase loader)
    public Book(String title, String filePath) {
        this.title = title;
        this.pathFile = filePath;
        this.authorName = "Unknown Author";
        this.category = "Uncategorized";
        this.totalPages = 0;
        this.lastReadPageNumber = 0;
        this.isFavourite = false;
        this.progressValue = 0.0;
        this.dateAdded = System.currentTimeMillis();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getTitle()                { return title; }
    public String getFilePath()             { return pathFile; }
    public String getAuthorName()           { return authorName; }
    public String getCategory()             { return category; }
    public int getTotalPages()              { return totalPages; }
    public int getLastReadPageNumber()      { return lastReadPageNumber; }
    public boolean isFavourite()            { return isFavourite; }
    public String getCoverPath()            { return coverPath; }
    public double getProgressValue()        { return progressValue; }
    public long getDateAdded()              { return dateAdded; }
    public Image getCoverImage()            { return coverImage; }

    /** Computed progress 0.0-1.0 from page numbers */
    public double getProgress() {
        if (totalPages <= 0) return 0.0;
        return (double) lastReadPageNumber / totalPages;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setTitle(String title)                      { this.title = title; }
    public void setFilePath(String path)                    { this.pathFile = path; }
    public void setAuthorName(String authorName)            { this.authorName = authorName; }
    public void setCategory(String category)                { this.category = category; }
    public void setTotalPages(int total)                    { this.totalPages = total; }
    public void setLastReadPageNumber(int last)             { this.lastReadPageNumber = last; }
    public void setFavouriteStatus(boolean fav)             { this.isFavourite = fav; }
    public void setCoverPath(String coverPath)              { this.coverPath = coverPath; }
    public void setProgressValue(double p)                  { this.progressValue = p; }
    public void setDateAdded(long dateAdded)                { this.dateAdded = dateAdded; }
    public void setCoverImage(Image coverImage)             { this.coverImage = coverImage; }
}