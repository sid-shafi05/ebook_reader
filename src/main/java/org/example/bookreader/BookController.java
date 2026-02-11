package org.example.bookreader;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import java.io.IOException;

public class BookController {
    private long sessionStartTime;
    private int sessionStartPage;
    private Book currentBook;
    private int currentPage;
    @FXML
    private ImageView pdfView;
    private PDFEngine engine;
    public void startSession(Book book){
        System.out.println("DEBUG: BookController received book: " + book.getTitle());
        this.currentBook=book;
        this.currentPage=book.getLastReadPageNumber();
        this.sessionStartTime=System.currentTimeMillis();
        this.sessionStartPage=currentPage;
        try{
            this.engine=new PDFEngine(book.getFilePath());
            renderCurrentPage();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private void renderCurrentPage(){
        if(engine!=null){
            pdfView.setImage(engine.getPageImage(currentPage));
        }
    }
    @FXML
    public void nextButtonLogic(){
        if(currentPage<engine.getPageCount()-1){
            currentPage++;
            renderCurrentPage();
        }
    }

    @FXML
    public void prevButtonLogic(){
        if(currentPage>0){
            currentPage--;
            renderCurrentPage();
        }
    }
    //when user closes the book or goees back to the library
    public void stopSession(){
        long endTime=System.currentTimeMillis();
        long seconds=(endTime-sessionStartTime)/1000;
        int pagesReadInThisSession=Math.abs(currentPage-sessionStartPage);
        SingleReadingEvent event=new SingleReadingEvent(java.time.LocalDate.now().toString(),currentBook.getTitle(),pagesReadInThisSession,seconds, currentBook.getCategory());
        StatsManagement.saveNewEvent(event);
        currentBook.setLastReadPageNumber(currentPage);
        List<Book> library=Library.loadBooks();
        Library.saveBookList(library);
        if(engine!=null){
            engine.close();
        }
    }
}
