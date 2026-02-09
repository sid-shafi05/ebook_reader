package org.example.bookreader;

public class Book {
    private String title;
    private String pathFile;
    private String authorName;
    private int totalPages;
    private int lastReadPageNumber;
    private boolean isFavourite;
    public Book(){}
    public Book(String title,String author,String path,int total){
        this.title=title;
        this.authorName=author;
        this.pathFile=path;
        this.totalPages=total;
    }
    //public setter-getters for data lookup and fetching
    public String getTitle(){
        return title;
    }
    public void setTitle(String tit){
        this.title=title;
    }
    public String getFilePath(){
        return pathFile;
    }
    public void setFilePath(String path){
        this.pathFile=path;
    }
    public void setAuthorName(String auth){
        this.authorName=auth;
    }
    public String getAuthorName(){
        return authorName;
    }
    public int getTotalPages(){
        return totalPages;
    }
    public void setTotalPages(int total){
        totalPages=total;
    }
    public int getLastReadPageNumber(){
        return lastReadPageNumber;
    }
    public void setLastReadPageNumber(int last){
        lastReadPageNumber=last;
    }
    public boolean Favourite(){
        return isFavourite;
    }
    public void setFavouriteStatus(boolean fav){
        isFavourite=fav;
    }
}
