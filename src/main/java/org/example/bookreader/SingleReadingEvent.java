package org.example.bookreader;

// on "date", "bookTitle" was read for "secondsRead" seconds, covering "pagesRead" pages, from "category"
// one object = one reading session (open book → close book)
// multiple sessions of the same book on the same day are stored as separate entries — StatsManagement adds them up
public class SingleReadingEvent {
    private String date;
    private String bookTitle;
    private int pagesRead;
    private long secondsRead;
    private String category;

    // empty constructor needed by Jackson to load from stats.json
    public SingleReadingEvent(){}

    public SingleReadingEvent(String date, String bookTitle, int pagesRead, long secondsRead, String category){
        this.date = date;
        this.bookTitle = bookTitle;
        this.pagesRead = pagesRead;
        this.secondsRead = secondsRead;
        this.category = category;
    }

    // getters — Jackson uses these to WRITE to stats.json
    public String getDate(){ return date; }
    public String getBookTitle(){ return bookTitle; }
    public int getPagesRead(){ return pagesRead; }
    public String getCategory(){ return category; }
    public long getSecondsRead(){ return secondsRead; }

    // setters — Jackson uses these to READ from stats.json
    public void setDate(String date){ this.date = date; }
    public void setBookTitle(String bookTitle){ this.bookTitle = bookTitle; }
    public void setPagesRead(int pagesRead){ this.pagesRead = pagesRead; }
    public void setCategory(String category){ this.category = category; }
    public void setSecondsRead(long secondsRead){ this.secondsRead = secondsRead; }
}
