package org.example.bookreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javafx.scene.image.Image;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String title;
    private String pathFile;
    private int totalPages;
    private int lastReadPageNumber;
    private boolean isFavourite;
   // private Image coverImage;  // ← ADD THIS
    private String category;
    private String coverPath;
    private double progressValue;

    public Book() {}

    // Updated constructor with coverImage
    public Book(String title, String path, int total,String category,double progress,String coverPath,int lastReadPageNumber) {
        this.title = title;
        this.pathFile = path;
        this.coverPath = coverPath;  // ← ADD THIS
        this.totalPages = total;
        this.lastReadPageNumber = 0;
        this.isFavourite = false;
        this.category=category;
        this.progressValue=progress;
        this.lastReadPageNumber=0;
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


    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public void setCategory(String category){this.category=category;}
    public String getCategory(){return category;}
    public void setProgressValue(double p){progressValue=p;}
    public double getProgressValue(){return progressValue;}
}