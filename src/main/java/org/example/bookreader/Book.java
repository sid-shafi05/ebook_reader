package org.example.bookreader;

public class Book {
    private String title;
    private String filepath;
    private boolean isFav;

    public Book(String title, String filepath)
    {
        this.title=title;
        this.filepath=filepath;
        this.isFav=false;

    }
    public String getTitle(){return title;}
    public String getFilepath(){return filepath;}
    public boolean getFav(){return isFav;}

    public void getTitle(String title){this.title=title;}
    public void getFilepath(String filepath){this.filepath=filepath;}
    public void getFav(boolean isFav){this.isFav=isFav;}
}
