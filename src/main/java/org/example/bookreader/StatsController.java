package org.example.bookreader;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
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

    // fx:id in FXML must match exactly
    @FXML private PieChart timePerCategoryChart;
    @FXML private LineChart<String, Number> timePerDayChart;

    public void initialize() {
        loadAllStats();
    }

    private void loadAllStats() {
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

    // PIE CHART — only top 5 valid categories by total time
    private void loadCategoryPieChart() {
        if (timePerCategoryChart == null) return;

        Map<String, Double> timePerCat = StatsManagement.getTimePerCategory();

        if (timePerCat.isEmpty()) {
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
        for (String cat : timePerCat.keySet()) {
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

        // bubble sort — descending by seconds (we store minutes as doubles from seconds/60)
        for (int i = 0; i < allCats.size() - 1; i++) {
            for (int j = 0; j < allCats.size() - 1 - i; j++) {
                double a = timePerCat.get(allCats.get(j));
                double b = timePerCat.get(allCats.get(j + 1));
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
            double mins = timePerCat.get(cat);
            // if less than 1 minute, show seconds instead so it doesn't say "0 min"
            String timeLabel;
            if (mins < 1.0) {
                int secs = (int) Math.round(mins * 60);
                timeLabel = secs + " sec";
            } else {
                timeLabel = (int) mins + " min";
            }
            String sliceLabel = cat + " (" + timeLabel + ")";
            slices.add(new PieChart.Data(sliceLabel, mins));
        }

        timePerCategoryChart.setData(slices);
        timePerCategoryChart.setTitle("");
        timePerCategoryChart.setLabelsVisible(true);
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

        // only show last 14 days
        if (sortedDates.size() > 14) {
            sortedDates = sortedDates.subList(sortedDates.size() - 14, sortedDates.size());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Minutes");

        for (String date : sortedDates) {
            double mins = timePerDay.get(date);
            series.getData().add(new XYChart.Data<>(date, mins));
        }

        timePerDayChart.getData().clear();
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

    @FXML
    public void onBackButtonClick() {
        System.out.println("Stats back button clicked");
    }
}
