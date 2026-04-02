package org.example.bookreader;

import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ReaderMenuHandler {
    private ImageView pdfView;
    private Button focusModeButton;
    private Button menuButton;
    private HBox sliderSection;
    private HBox controlsSection;
    private boolean focusModeActive = false;
    private ContextMenu currentMenu;
    private Runnable onNotebookToggle;

    public ReaderMenuHandler(ImageView pdfView, Button focusModeButton, Button menuButton,
                             HBox sliderSection, HBox controlsSection) {
        this.pdfView = pdfView;
        this.focusModeButton = focusModeButton;
        this.menuButton = menuButton;
        this.sliderSection = sliderSection;
        this.controlsSection = controlsSection;
    }

    public void setOnNotebookToggle(Runnable callback) {
        this.onNotebookToggle = callback;
    }

    public void toggleFocusMode() {
        focusModeActive = !focusModeActive;
        if (focusModeActive) {
            if (sliderSection != null)   sliderSection.setVisible(false);
            if (controlsSection != null) controlsSection.setVisible(false);
            if (focusModeButton != null)
                focusModeButton.setStyle(
                        "-fx-background-color: #1a1508; -fx-text-fill: #c8a96e;" +
                                "-fx-border-color: #c8a96e; -fx-border-width: 1;" +
                                "-fx-background-radius: 7; -fx-border-radius: 7;"
                );
        } else {
            if (sliderSection != null)   sliderSection.setVisible(true);
            if (controlsSection != null) controlsSection.setVisible(true);
            if (focusModeButton != null) focusModeButton.setStyle("");
        }
    }

    // Kept as no-ops so any existing FXML action bindings don't break
    public void setColorFilterNormal() { if (pdfView != null) pdfView.setEffect(null); }
    public void setColorFilterDark()   { }
    public void setColorFilterSepia()  {
        ColorAdjust sepia = new ColorAdjust();
        sepia.setHue(-0.1);
        sepia.setBrightness(0.08);
        sepia.setContrast(0.2);
        sepia.setSaturation(-0.9);
        ColorInput warm = new ColorInput(0, 0, 2000, 2000, Color.web("#f5deb3"));
        if (pdfView != null) pdfView.setEffect(new Blend(BlendMode.MULTIPLY, sepia, warm));
    }

    public void showColorFilterMenu() {
        if (currentMenu != null && currentMenu.isShowing()) {
            currentMenu.hide();
            currentMenu = null;
            return;
        }

        ContextMenu menu = new ContextMenu();

        MenuItem notebookItem = new MenuItem("✎  Notebook");
        notebookItem.setOnAction(e -> {
            menu.hide();
            currentMenu = null;
            if (onNotebookToggle != null) onNotebookToggle.run();
        });

        menu.getItems().add(notebookItem);

        if (menuButton != null) {
            javafx.scene.Scene scene = menuButton.getScene();
            if (scene != null) {
                String css = getClass()
                        .getResource("/org/example/bookreader/application.css")
                        .toExternalForm();
                if (!scene.getStylesheets().contains(css))
                    scene.getStylesheets().add(css);
            }
            Bounds bounds = menuButton.localToScreen(menuButton.getBoundsInLocal());
            menu.show(menuButton, bounds.getCenterX(), bounds.getCenterY() + 20);
            currentMenu = menu;
        }
    }
}