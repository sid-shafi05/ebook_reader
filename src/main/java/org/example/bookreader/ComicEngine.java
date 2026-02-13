package org.example.bookreader;

import javafx.scene.image.Image;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicEngine {
    private List<String> imageFiles;
    private ZipFile zipFile;

    public ComicEngine(String filePath) throws IOException {
        this.zipFile = new ZipFile(filePath);
        this.imageFiles = new ArrayList<>();

        // Load all image files from the zip
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName().toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                    name.endsWith(".png") || name.endsWith(".gif")) {
                imageFiles.add(entry.getName());
            }
        }

        // Sort files naturally
        Collections.sort(imageFiles);
    }

    public Image getPage(int pageNumber) {
        if (pageNumber < 0 || pageNumber >= imageFiles.size()) {
            return null;
        }

        try {
            ZipEntry entry = zipFile.getEntry(imageFiles.get(pageNumber));
            InputStream inputStream = zipFile.getInputStream(entry);
            return new Image(inputStream);
        } catch (IOException e) {
            System.err.println("Error loading comic page: " + e.getMessage());
            return null;
        }
    }

    public int getTotalPages() {
        return imageFiles.size();
    }

    // Close zip file when done - ADD THIS METHOD
    public void close() {
        try {
            if (zipFile != null) {
                zipFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}