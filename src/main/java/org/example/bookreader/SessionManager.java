package org.example.bookreader;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.List;

public class SessionManager {
    private long sessionStartTime;
    private int sessionStartPage;
    private int highestPageReached;
    private Timeline readingTimer;
    private int readingSeconds = 0;
    private Book currentBook;

    public void startSession(Book book, int startPage) {
        this.currentBook = book;
        this.sessionStartTime = System.currentTimeMillis();
        this.sessionStartPage = startPage;
        this.highestPageReached = startPage;
        startReadingTimer();
    }

    public void updateHighestPage(int currentPage) {
        if (currentPage > highestPageReached) {
            highestPageReached = currentPage;
        }
    }

    public void stopSession(int currentPage, int totalPages, FileTypeManager fileTypeManager) {
        long endTime = System.currentTimeMillis();
        long seconds = (endTime - sessionStartTime) / 1000;
        int pagesReadThisSession = highestPageReached - sessionStartPage;

        double newProgress;
        if (highestPageReached >= totalPages - 1) {
            newProgress = 1.0;
        } else {
            newProgress = (double) highestPageReached / (totalPages - 1);
        }

        SingleReadingEvent event = new SingleReadingEvent(
                java.time.LocalDate.now().toString(),
                currentBook.getTitle(),
                pagesReadThisSession,
                seconds,
                currentBook.getCategory()
        );
        StatsManagement.saveNewEvent(event);

        currentBook.setLastReadPageNumber(currentPage);
        currentBook.setProgressValue(newProgress);

        List<Book> library = Library.loadBooks();
        for (Book b : library) {
            if (b.getFilePath().equals(currentBook.getFilePath())) {
                b.setLastReadPageNumber(currentPage);
                b.setProgressValue(newProgress);
            }
        }
        Library.saveBookList(library);
        stopReadingTimer();
    }

    private void startReadingTimer() {
        readingSeconds = 0;
        readingTimer = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> readingSeconds++)
        );
        readingTimer.setCycleCount(Timeline.INDEFINITE);
        readingTimer.play();
    }

    private void stopReadingTimer() {
        if (readingTimer != null) readingTimer.stop();
    }

    public int getHighestPageReached() { return highestPageReached; }
    public Book getCurrentBook() { return currentBook; }
}