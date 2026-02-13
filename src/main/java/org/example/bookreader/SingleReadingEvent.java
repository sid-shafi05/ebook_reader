package org.example.bookreader;
//on "date", "x" book was read for "y" seconds
public class SingleReadingEvent {
    private String date;
    private String bookTitle;
    private int pagesRead;
    private long secondsRead;
    private String category;
    public SingleReadingEvent(){}
    public SingleReadingEvent(String date,String bookTitle,int pagesRead,long secondsRead,String category){
        this.date=date;
        this.bookTitle=bookTitle;
        this.pagesRead=pagesRead;
        this.secondsRead=secondsRead;
        this.category=category;
    }
    //setter-getters for info
    public String getDate(){
        return date;
    }
    public int getPagesRead(){
        return pagesRead;

    }
    public String getCategory(){
        return category;
    }

    public long getSecondsRead() {
        return secondsRead;
    }
}
