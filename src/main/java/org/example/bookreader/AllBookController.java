package org.example.bookreader;

import com.fasterxml.jackson.databind.annotation.JsonTypeResolver;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AllBookController {
    @FXML private FlowPane bookGrid;

    private String currentSort="title";
    private String currentFilter="";

    public void initialize() throws IOException {loadBooks();}
    public void setSort(String sort) throws IOException {
        this.currentSort = sort;
        loadBooks();
    }
    public void setFilter(String filter) throws IOException {
        this.currentFilter = filter;
        loadBooks();
    }
    public void loadBooks() throws IOException {
        //sort and load books
        if(bookGrid==null) return;
        bookGrid.getChildren().clear();

        List<Book> books= new ArrayList<>(Library.loadBooks());
        // Apply filter
        if(!currentFilter.isEmpty()) {
            books = books.stream()
                    .filter(b -> b.getTitle().toLowerCase().contains(currentFilter)
                            || b.getCategory().toLowerCase().contains(currentFilter))
                    .collect(Collectors.toList());

        }

        switch(currentSort){
            case "title":
                books.sort((a,b)->a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case "date":
                books.sort((a,b)->Long.compare(b.getDateAdded(),a.getDateAdded()));
                break;
            case "progress":
                books.sort((a,b)->Double.compare(b.getProgressValue(),a.getProgressValue()));
                break;
        }

        if(books.isEmpty())
        {
            Label lbl=new Label(currentFilter.isEmpty()
                    ? "No books in library. Add some!"
                    : "No books match the filter.");
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa; -fx-padding: 50;");
            bookGrid.getChildren().add(lbl);
            return;
        }

        for(Book b:books)
        {
            Controller mainctrl= Main.getMainController();
            if(mainctrl!=null) bookGrid.getChildren().add(mainctrl.createBookTile(b));

        }

    }
    public void refreshBooks() throws IOException {loadBooks();}
}