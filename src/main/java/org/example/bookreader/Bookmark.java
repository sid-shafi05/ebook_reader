package org.example.bookreader;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a bookmark in a book.
 * Stores the page number and optional notes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Bookmark {
    private String bookPath;  // Path to the book file
    private int pageNumber;   // Bookmarked page
    private String notes;     // Optional notes
    private long dateCreated; // When bookmark was created

    public Bookmark() {}

    public Bookmark(String bookPath, int pageNumber) {
        this.bookPath = bookPath;
        this.pageNumber = pageNumber;
        this.dateCreated = System.currentTimeMillis();
    }

    public Bookmark(String bookPath, int pageNumber, String notes) {
        this(bookPath, pageNumber);
        this.notes = notes;
    }

    // Getters
    public String getBookPath() { return bookPath; }
    public int getPageNumber() { return pageNumber; }
    public String getNotes() { return notes; }
    public long getDateCreated() { return dateCreated; }

    // Setters
    public void setBookPath(String bookPath) { this.bookPath = bookPath; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setDateCreated(long dateCreated) { this.dateCreated = dateCreated; }

    @Override
    public String toString() {
        return "Bookmark{" +
                "bookPath='" + bookPath + '\'' +
                ", pageNumber=" + pageNumber +
                ", notes='" + notes + '\'' +
                '}';
    }
}

