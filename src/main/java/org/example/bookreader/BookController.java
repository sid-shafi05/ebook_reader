package org.example.bookreader;
import javafx.scene.layout.HBox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.*;
import java.util.List;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import java.io.IOException;

public class BookController {
    public HBox navigationBar;
    private long sessionStartTime;
    private int sessionStartPage;
    private Book currentBook;
    private int currentPage;
    private int pageProgress;

    @FXML private ImageView pdfView;
    @FXML private Label pageNumberLabel;

    private PDFEngine engine;

    public void startSession(Book book){
        System.out.println("DEBUG: BookController received book: " + book.getTitle());
        this.currentBook=book;
        this.currentPage=book.getLastReadPageNumber();
        this.sessionStartTime=System.currentTimeMillis();
        this.sessionStartPage=currentPage;
        this.pageProgress=currentPage;
        try{
            this.engine=new PDFEngine(book.getFilePath());
            renderCurrentPage();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    @FXML
    private void renderCurrentPage(){
        if(engine!=null){
            pdfView.setImage(engine.renderingPage( currentPage));
            if (pageNumberLabel != null) {
                pageNumberLabel.setText("Page " + (currentPage + 1) + " of " + engine.getPageCount());
            }
        }
    }
    @FXML
    public void nextButtonLogic(){
        if(currentPage<engine.getPageCount()-1){
            currentPage++;
            pageProgress++;
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

    @FXML
    public void onBackButtonClick() {
        //SAVE YOUR READING PROGRESS (Crucial for  stats/progress bar)
        stopSession();


        if (engine != null) {
            engine.close(); // Stops the PDF file handle from leaking
        }

        // RE-OPEN THE LIBRARY SCREEN IN THE MAIN WINDOW
        // This calls the main controller to swap the content area back to the library
        Main.getMainController().changeToAllBooks();
    }

    //when user closes the book or goes back to the library
    public void stopSession(){
        long endTime=System.currentTimeMillis();
        long seconds=(endTime-sessionStartTime)/1000;
        int pagesReadInThisSession=Math.abs(pageProgress-sessionStartPage);
        currentBook.setProgressValue((double)(pagesReadInThisSession/currentBook.getTotalPages()));
        SingleReadingEvent event=new SingleReadingEvent(java.time.LocalDate.now().toString(),currentBook.getTitle(),pagesReadInThisSession,seconds, currentBook.getCategory());
        StatsManagement.saveNewEvent(event);
        currentBook.setLastReadPageNumber(currentPage);
        List<Book> library=Library.loadBooks();
        for(Book b : library) {
            if (b.getFilePath().equals(currentBook.getFilePath())) {
                b.setLastReadPageNumber(currentPage);
                b.setProgressValue((double)(pagesReadInThisSession/currentBook.getTotalPages()));
                // Update progress bar data here too
            }
        }

        System.out.println("savingggg");
        Library.saveBookList(library);
        if(engine!=null){
            engine.close();
        }
    }




}
