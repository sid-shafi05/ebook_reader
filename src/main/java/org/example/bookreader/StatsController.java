package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatsController {

    @FXML private Label totalPagesReadLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label avgTimePerDayLabel;

    // Today's progress labels
    @FXML private Label todayTimeLabel;
    @FXML private Label targetLabel;
    @FXML private Label progressStatusLabel;

    // fx:id in FXML must match exactly
    @FXML private PieChart timePerCategoryChart;
    @FXML private LineChart<String, Number> timePerDayChart;

    // time range buttons
    @FXML private Button btn7Days;
    @FXML private Button btn14Days;
    @FXML private Button btn30Days;

    private int selectedDays = 14; // default

    public void initialize() {
        loadAllStats();
    }

    // called when stats page becomes visible to refresh data
    public void onPageShown() {
        loadAllStats();
        updateButtonStyles();
    }

    private void loadAllStats() {
        // TODAY'S PROGRESS vs TARGET
        loadTodayProgress();

        // total pages
        int totalPages = StatsManagement.getTotalPagesRead();
        if (totalPagesReadLabel != null) {
            totalPagesReadLabel.setText(totalPages + " pages");
        }

        // total time
        long totalSeconds = StatsManagement.getTotalTimeInSeconds();
        double totalMinutes = totalSeconds / 60.0;
        if (totalTimeLabel != null) {
            totalTimeLabel.setText(formatTime(totalMinutes));
        }

        // avg per day
        int daysRead = StatsManagement.getTotalDaysRead();
        double avgMinutes = daysRead > 0 ? totalMinutes / daysRead : 0;
        if (avgTimePerDayLabel != null) {
            avgTimePerDayLabel.setText(formatTime(avgMinutes));
        }

        loadCategoryPieChart();
        loadDailyLineChart();
    }

    // PIE CHART — only top 5 valid categories by total time, filtered by selected time range
    private void loadCategoryPieChart() {
        if (timePerCategoryChart == null) return;

        // Get time per day data
        Map<String, Double> timePerDay = StatsManagement.getTimeSpentPerDay();

        if (timePerDay.isEmpty()) {
            return;
        }

        // Calculate time per category for the selected time range
        String today = java.time.LocalDate.now().toString();
        java.time.LocalDate todayDate = java.time.LocalDate.parse(today);
        java.time.LocalDate startDate = todayDate.minusDays(selectedDays);

        Map<String, Double> timePerCatFiltered = new java.util.HashMap<>();

        // Go through all events and filter by date range and category
        List<SingleReadingEvent> allEvents = StatsManagement.getAllEvents();
        for (SingleReadingEvent event : allEvents) {
            java.time.LocalDate eventDate = java.time.LocalDate.parse(event.getDate());

            // Check if event is within selected time range
            if (eventDate.isEqual(startDate) || eventDate.isAfter(startDate)) {
                String category = event.getCategory();
                double timeSpent = event.getSecondsRead() / 60.0; // convert to minutes

                timePerCatFiltered.put(
                    category,
                    timePerCatFiltered.getOrDefault(category, 0.0) + timeSpent
                );
            }
        }

        if (timePerCatFiltered.isEmpty()) {
            return;
        }

        // these are not real genres — skip them
        List<String> invalidCats = new ArrayList<>();
        invalidCats.add("General");
        invalidCats.add("Other");
        invalidCats.add("Unknown");
        invalidCats.add("None");
        invalidCats.add("Uncategorized");

        // put valid categories in a list
        List<String> allCats = new ArrayList<>();
        for (String cat : timePerCatFiltered.keySet()) {
            boolean isInvalid = false;
            for (String bad : invalidCats) {
                if (cat.equalsIgnoreCase(bad)) {
                    isInvalid = true;
                    break;
                }
            }
            if (!isInvalid) {
                allCats.add(cat);
            }
        }

        if (allCats.isEmpty()) {
            return;
        }

        // bubble sort — descending by minutes
        for (int i = 0; i < allCats.size() - 1; i++) {
            for (int j = 0; j < allCats.size() - 1 - i; j++) {
                double a = timePerCatFiltered.get(allCats.get(j));
                double b = timePerCatFiltered.get(allCats.get(j + 1));
                if (a < b) {
                    String temp = allCats.get(j);
                    allCats.set(j, allCats.get(j + 1));
                    allCats.set(j + 1, temp);
                }
            }
        }

        // only take top 5
        int limit = Math.min(5, allCats.size());

        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();

        for (int i = 0; i < limit; i++) {
            String cat = allCats.get(i);
            double mins = timePerCatFiltered.get(cat);
            // if less than 1 minute, show seconds instead so it doesn't say "0 min"
            String timeLabel;
            if (mins < 1.0) {
                int secs = (int) Math.round(mins * 60);
                timeLabel = secs + " sec";
            } else {
                timeLabel = (int) mins + " min";
            }
            // legend will show category (time), pie slices show just category name
            String legendLabel = cat + " (" + timeLabel + ")";
            slices.add(new PieChart.Data(legendLabel, mins));
        }

        timePerCategoryChart.setData(slices);
        timePerCategoryChart.setTitle("");
        timePerCategoryChart.setLabelsVisible(false); // hide labels on pie slices
        timePerCategoryChart.setLegendVisible(true);
    }

    // LINE CHART — X axis = date (sorted), Y axis = minutes read that day
    private void loadDailyLineChart() {
        if (timePerDayChart == null) return;

        Map<String, Double> timePerDay = StatsManagement.getTimeSpentPerDay();

        if (timePerDay.isEmpty()) {
            return;
        }

        // sort dates so they appear in order on the X axis
        List<String> sortedDates = new ArrayList<>(timePerDay.keySet());
        Collections.sort(sortedDates);

        // only show last N days (or all if selectedDays is very large)
        if (sortedDates.size() > selectedDays) {
            sortedDates = sortedDates.subList(sortedDates.size() - selectedDays, sortedDates.size());
        }

        // CLEAR everything first
        timePerDayChart.getData().clear();

        // rebuild X-axis with correct categories
        javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) timePerDayChart.getXAxis();
        if (xAxis != null) {
            xAxis.getCategories().clear();
            xAxis.setCategories(FXCollections.observableArrayList(sortedDates));
            xAxis.setTickLabelRotation(45);
        }

        // rebuild Y-axis
        javafx.scene.chart.NumberAxis yAxis = (javafx.scene.chart.NumberAxis) timePerDayChart.getYAxis();
        double maxMinutes = 0;

        // calculate max to set Y-axis bounds
        for (String date : sortedDates) {
            double mins = timePerDay.get(date);
            if (mins > maxMinutes) {
                maxMinutes = mins;
            }
        }

        if (yAxis != null) {
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(Math.max(maxMinutes * 1.1, 5));
            yAxis.setTickUnit(Math.max(1, Math.ceil(maxMinutes / 10)));
            yAxis.setAutoRanging(false);
        }

        // NOW add the data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Minutes");

        for (String date : sortedDates) {
            double mins = timePerDay.get(date);
            double displayValue = Math.round(mins * 100.0) / 100.0;
            series.getData().add(new XYChart.Data<>(date, displayValue));
        }

        timePerDayChart.getData().add(series);
        timePerDayChart.setLegendVisible(false);
    }

    private String formatTime(double minutes) {
        if (minutes < 60) {
            return String.format("%.0f min", minutes);
        } else {
            double hours = minutes / 60.0;
            return String.format("%.1f hrs", hours);
        }
    }

    // TIME RANGE BUTTON HANDLERS
    @FXML
    public void onTimeRange7Days() {
        System.out.println("DEBUG: 7 Days button clicked");
        selectedDays = 7;
        loadCategoryPieChart();
        loadDailyLineChart();
        updateButtonStyles();
    }

    @FXML
    public void onTimeRange14Days() {
        System.out.println("DEBUG: 14 Days button clicked");
        selectedDays = 14;
        loadCategoryPieChart();
        loadDailyLineChart();
        updateButtonStyles();
    }

    @FXML
    public void onTimeRange30Days() {
        System.out.println("DEBUG: 30 Days button clicked");
        selectedDays = 30;
        loadCategoryPieChart();
        loadDailyLineChart();
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        // null check in case buttons aren't injected yet
        if (btn7Days == null || btn14Days == null || btn30Days == null) {
            return;
        }

        // reset all buttons
        btn7Days.setStyle("-fx-padding: 8 16; -fx-font-size: 11; -fx-background-color: #0f3460; -fx-text-fill: #e0e0ff; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        btn14Days.setStyle("-fx-padding: 8 16; -fx-font-size: 11; -fx-background-color: #0f3460; -fx-text-fill: #e0e0ff; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        btn30Days.setStyle("-fx-padding: 8 16; -fx-font-size: 11; -fx-background-color: #0f3460; -fx-text-fill: #e0e0ff; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");

        // highlight active button
        String activeStyle = "-fx-padding: 8 16; -fx-font-size: 11; -fx-background-color: #4f6cff; -fx-text-fill: #ffffff; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;";

        if (selectedDays == 7) {
            btn7Days.setStyle(activeStyle);
        } else if (selectedDays == 14) {
            btn14Days.setStyle(activeStyle);
        } else if (selectedDays == 30) {
            btn30Days.setStyle(activeStyle);
        }
    }

    // Load today's progress and compare with daily target
    private void loadTodayProgress() {
        String today = java.time.LocalDate.now().toString();
        Map<String, Double> timePerDay = StatsManagement.getTimeSpentPerDay();

        // Get time read today in minutes
        double todayMinutes = timePerDay.getOrDefault(today, 0.0);

        // Get daily target in minutes
        int dailyTarget = Controller.getDailyTarget();

        // Calculate progress percentage
        int progressPercent = (int) ((todayMinutes / dailyTarget) * 100);
        if (progressPercent > 100) {
            progressPercent = 100; // Cap at 100%
        }

        // Update UI labels
        if (todayTimeLabel != null) {
            if (todayMinutes < 1.0) {
                int secs = (int) Math.round(todayMinutes * 60);
                todayTimeLabel.setText(secs + " sec");
            } else {
                todayTimeLabel.setText((int) todayMinutes + " min");
            }
        }

        if (targetLabel != null) {
            targetLabel.setText(dailyTarget + " min");
        }

        if (progressStatusLabel != null) {
            String statusText = progressPercent + "% Complete";
            progressStatusLabel.setText(statusText);

            // Change color based on progress
            if (progressPercent >= 100) {
                progressStatusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #4caf50;"); // Green
            } else if (progressPercent >= 50) {
                progressStatusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ffb74d;"); // Orange
            } else {
                progressStatusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ff6b6b;"); // Red
            }
        }
    }

    @FXML
    public void onBackButtonClick() {
        System.out.println("Stats back button clicked");
    }
}
