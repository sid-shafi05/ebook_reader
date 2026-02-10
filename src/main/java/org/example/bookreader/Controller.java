package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

public class Controller {

    @FXML
    private AnchorPane contentArea;

    //Buttons
    @FXML
    private Button allBtn;
    @FXML
    private Button favBtn;
    @FXML
    private Button catBtn;
    @FXML
    private Button setBtn;
    @FXML
    private Button helpBtn;

    //Sort Labels
    @FXML
    private Label titleLabel;
    @FXML
    private Label progressLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label dateLabel;

    @FXML
    //Default Initialization
    public void initialize() {
        loadPage("allbooks.fxml");
        sortByTitle();
        if (allBtn != null) {
            setActiveStyle(allBtn);
        } else {
            System.out.println("DEBUG: allBtn is still null. Check Scene Builder fx:id!");
        }
    }

    //Changing Buttons
    @FXML
    public void changeToAllBooks() {
        loadPage("allbooks.fxml");
        setActiveStyle(allBtn);
    }

    @FXML
    public void changetoFavourites() {
        loadPage("fav.fxml");
        setActiveStyle(favBtn);
    }

    public void changeToCategories() {
        loadPage("cat.fxml");
        setActiveStyle(catBtn);
    }

    //Changing buttonStyle
    private void setActiveStyle(Button clickedButton) {

        allBtn.getStyleClass().remove("active");
        favBtn.getStyleClass().remove("active");
        catBtn.getStyleClass().remove("active");
        helpBtn.getStyleClass().remove("active");
        setBtn.getStyleClass().remove("active");


        clickedButton.getStyleClass().add("active");
    }

    //Loading the button related page
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/bookreader/" + fxmlFile));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);


            AnchorPane.setTopAnchor(page, 0.0);
            AnchorPane.setBottomAnchor(page, 0.0);
            AnchorPane.setLeftAnchor(page, 0.0);
            AnchorPane.setRightAnchor(page, 0.0);
        } catch (IOException e) {
            System.out.println("Error loading: " + fxmlFile);
        }
    }

    //Setting up sort styles
    public void sortByTitle() {
        setActiveSort(titleLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    public void sortByAuthor() {
        setActiveSort(authorLabel);
        titleLabel.setText("Title");
        dateLabel.setText("Date ");
        authorLabel.setText("Author");
        progressLabel.setText("Progress");
    }

    public void sortByDate() {
        setActiveSort(dateLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author ");
        progressLabel.setText("Progress");
    }

    public void sortByProgress() {
        setActiveSort(progressLabel);
        titleLabel.setText("Title ");
        dateLabel.setText("Date");
        authorLabel.setText("Author");
        progressLabel.setText("Progress ");
    }

    public void setActiveSort(Label active) {
        titleLabel.getStyleClass().remove("label-color-active");
        authorLabel.getStyleClass().remove("label-color-active");
        progressLabel.getStyleClass().remove("label-color-active");
        dateLabel.getStyleClass().remove("label-color-active");

        active.getStyleClass().add("label-color-active");
    }



}