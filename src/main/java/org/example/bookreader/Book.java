package org.example.bookreader;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String title;
    private String pathFile;
    //private String authorName;
    private int totalPages;
    private int lastReadPageNumber;
    private boolean isFavourite;
    private String category;//academic,comic,fiction,novel etc
    private String coverPath;
    private double progress;

    public Book(){}
    public Book(String title,String path,int total,String category,String coverPath){
        this.title=title;
       // this.authorName=author;
        this.pathFile=path;
        this.totalPages=total;
        this.category=category;
        this.isFavourite=false;
        this.coverPath=coverPath;
    }
    //progress bar logic
    public double getProgress(){
        if(totalPages<=0){
            return 0;
        }
        return (double)lastReadPageNumber/totalPages;
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
  //  public void setAuthorName(String auth){
    //    this.authorName=auth;
    //}
    //public String getAuthorName(){
      //  return authorName;
    //}
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
    public void setCategory(String cat){
        category=cat;
    }
    public String getCategory(){
        return category;
    }
    public String getCoverPath(){
        return coverPath;
    }
    public void setCoverPath(String cover){
        coverPath=cover;
    }
}
