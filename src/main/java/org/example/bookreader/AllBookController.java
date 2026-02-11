package org.example.bookreader;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.io.IOException;

public class AllBookController {
    void changeToAllBooks() throws IOException {
        FXMLLoader loadAll= new FXMLLoader(getClass().getResource("/org/example/bookreader/allbooks.fxml"));
        Parent view=loadAll.load();

    }
}
