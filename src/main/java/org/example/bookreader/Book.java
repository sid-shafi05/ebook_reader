package org.example.bookreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String title;
    private String pathFile;
    private int totalPages;
    private int lastReadPageNumber;

    // renamed from isFavourite to favourite — Jackson was stripping "is" and
    // couldn't match the setter "setFavouriteStatus", so it always loaded as false
    @JsonProperty("favourite")
    private boolean favourite;

    private String category;
    private String coverPath;
    private double progressValue;
    private long dateAdded;

    public Book() {}

    public Book(String title, String path, int total, String category, double progress, String coverPath, int lastReadPageNumber) {
        this.title = title;
        this.pathFile = path;
        this.coverPath = coverPath;
        this.totalPages = total;
        this.lastReadPageNumber = 0;
        this.favourite = false;
        this.category = category;
        this.progressValue = progress;
        this.dateAdded = System.currentTimeMillis();
    }

    // Getters
    public String getTitle() { return title; }
    public String getFilePath() { return pathFile; }
    public int getTotalPages() { return totalPages; }
    public int getLastReadPageNumber() { return lastReadPageNumber; }

    @JsonProperty("favourite")
    public boolean isFavourite() { return favourite; }

    public String getCoverPath() { return coverPath; }
    public String getCategory() { return category; }
    public double getProgressValue() { return progressValue; }
    public long getDateAdded() { return dateAdded; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setFilePath(String path) { this.pathFile = path; }
    public void setTotalPages(int total) { this.totalPages = total; }
    public void setLastReadPageNumber(int last) { this.lastReadPageNumber = last; }

    // setter name matches the field so Jackson can call it during deserialization
    @JsonProperty("favourite")
    public void setFavourite(boolean fav) { this.favourite = fav; }


    public void setFavouriteStatus(boolean fav) { this.favourite = fav; }

    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
    public void setCategory(String category) { this.category = category; }
    public void setProgressValue(double p) { this.progressValue = p; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }
}