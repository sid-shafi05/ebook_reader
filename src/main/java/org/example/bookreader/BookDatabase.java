package org.example.bookreader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookDatabase {
    private static BookDatabase instance;
    private ObservableList<Book> books;
    private static final String DATABASE_FILE = "books_database.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    private BookDatabase() {
        books = FXCollections.observableArrayList();
        loadFromFile();
    }

    public static BookDatabase getInstance() {
        if (instance == null) {
            instance = new BookDatabase();
        }
        return instance;
    }

    // Load books from JSON file
    private void loadFromFile() {
        File file = new File(DATABASE_FILE);
        if (!file.exists()) {
            System.out.println("No database file found. Starting fresh.");
            return;
        }

        try {
            List<BookData> bookDataList = mapper.readValue(file, new TypeReference<List<BookData>>() {});

            for (BookData data : bookDataList) {
                Book book = new Book(data.title, data.filePath);
                book.setAuthorName(data.authorName);
                book.setCategory(data.category);
                book.setTotalPages(data.totalPages);
                book.setLastReadPageNumber(data.lastReadPageNumber);
                book.setFavouriteStatus(data.isFavourite);
                // Note: Cover images will be regenerated when needed
                books.add(book);
            }

            System.out.println("Loaded " + books.size() + " books from database");

        } catch (IOException e) {
            System.err.println("Error loading database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save books to JSON file
    private void saveToFile() {
        try {
            List<BookData> bookDataList = new ArrayList<>();

            for (Book book : books) {
                BookData data = new BookData();
                data.title = book.getTitle();
                data.filePath = book.getFilePath();
                data.authorName = book.getAuthorName();
                data.category = book.getCategory();
                data.totalPages = book.getTotalPages();
                data.lastReadPageNumber = book.getLastReadPageNumber();
                data.isFavourite = book.isFavourite();
                bookDataList.add(data);
            }

            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(DATABASE_FILE), bookDataList);
            System.out.println("Saved " + books.size() + " books to database");

        } catch (IOException e) {
            System.err.println("Error saving database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addBook(Book book) {
        books.add(book);
        saveToFile(); // Auto-save when book is added
        System.out.println("Book added to database: " + book.getTitle());
        System.out.println("Total books in database: " + books.size());
    }

    public void removeBook(Book book) {
        books.remove(book);
        saveToFile(); // Auto-save when book is removed
    }

    public void updateBook(Book book) {
        // Find and update the book
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getFilePath().equals(book.getFilePath())) {
                books.set(i, book);
                break;
            }
        }
        saveToFile(); // Auto-save when book is updated
        System.out.println("Book updated: " + book.getTitle() + " (Last page: " + book.getLastReadPageNumber() + ")");
    }

    public ObservableList<Book> getAllBooks() {
        return books;
    }

    public int getBookCount() {
        return books.size();
    }

    public Book getBookByPath(String filePath) {
        for (Book book : books) {
            if (book.getFilePath().equals(filePath)) {
                return book;
            }
        }
        return null;
    }

    // Inner class for JSON serialization (without Image field)
    private static class BookData {
        public String title;
        public String filePath;
        public String authorName;
        public String category;
        public int totalPages;
        public int lastReadPageNumber;
        public boolean isFavourite;
    }
}