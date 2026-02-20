package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.List;

public class FavController {
    @FXML private FlowPane favGrid;
    public void initialize() {
        loadFavs();
    }
    public void loadFavs() {
        if(favGrid==null) return;
        favGrid.getChildren().clear();
        List<Book> favBooks=Library.getFavouriteBooks();

        if(favBooks.isEmpty())
        {
            Label lbl=new Label("No favourite books yet. Mark some as favourite!");
            lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #aaaaaa; -fx-padding: 50; -fx-text-alignment: center;");
            favGrid.getChildren().add(lbl);
            return;
        }

        Controller mainController = Main.getMainController();
        for(Book b : favBooks) {
            try{
                if(mainController !=null)
                    favGrid.getChildren().add(mainController.createBookTile(b));
            }catch(Exception e){
                System.err.println("Error loading book tile: "+e.getMessage());
                e.printStackTrace();
            }
            }
        }
}
