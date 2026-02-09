package org.example.bookreader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class AllBookController {
    void changeToAllBooks() throws IOException {
        FXMLLoader loadAll= new FXMLLoader(getClass().getResource("/org/example/bookreader/allbooks.fxml"));
        Parent view=loadAll.load();

    }
}
