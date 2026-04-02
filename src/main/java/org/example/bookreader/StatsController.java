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
    @FXML private Label todayTimeLabel;
    @FXML private Label targetLabel;
    @FXML private Label progressStatusLabel;
    @FXML private PieChart timePerCategoryChart;
    @FXML private LineChart<String, Number> timePerDayChart;
    @FXML private Button btn7Days;
    @FXML private Button btn14Days;
    @FXML private Button btn30Days;

    private int selectedDays = 14;

    public void initialize() { loadAllStats(); }

    public void onPageShown() {
        loadAllStats();
        updateButtonStyles();
    }

    private void loadAllStats() {
        loadTodayProgress();

        int totalPages = StatsManagement.getTotalPagesRead();
        if (totalPagesReadLabel != null)
            totalPagesReadLabel.setText(totalPages + " pages");

        long totalSeconds = StatsManagement.getTotalTimeInSeconds();
        double totalMinutes = totalSeconds / 60.0;
        if (totalTimeLabel != null)
            totalTimeLabel.setText(formatTime(totalMinutes));

        int daysRead = StatsManagement.getTotalDaysRead();
        double avgMinutes = daysRead > 0 ? totalMinutes / daysRead : 0;
        if (avgTimePerDayLabel != null)
            avgTimePerDayLabel.setText(formatTime(avgMinutes));

        loadCategoryPieChart();
        loadDailyLineChart();
    }

    private void loadCategoryPieChart() {
        if (timePerCategoryChart == null) return;

        java.time.LocalDate todayDate = java.time.LocalDate.now();
        java.time.LocalDate startDate = todayDate.minusDays(selectedDays);

        Map<String, Double> timePerCatFiltered = new java.util.HashMap<>();
        List<SingleReadingEvent> allEvents = StatsManagement.getAllEvents();

        for (SingleReadingEvent event : allEvents) {
            java.time.LocalDate eventDate = java.time.LocalDate.parse(event.getDate());
            if (!eventDate.isBefore(startDate)) {
                String category = event.getCategory();
                double timeSpent = event.getSecondsRead() / 60.0;
                timePerCatFiltered.merge(category, timeSpent, Double::sum);
            }
        }

        if (timePerCatFiltered.isEmpty()) return;

        List<String> invalidCats = List.of("General", "Other", "Unknown", "None", "Uncategorized");
        List<String> allCats = new ArrayList<>();
        for (String cat : timePerCatFiltered.keySet()) {
            boolean isInvalid = false;
            for (String bad : invalidCats) {
                if (cat.equalsIgnoreCase(bad)) { isInvalid = true; break; }
            }
            if (!isInvalid) allCats.add(cat);
        }

        if (allCats.isEmpty()) return;

        // Bubble sort descending
        for (int i = 0; i < allCats.size() - 1; i++) {
            for (int j = 0; j < allCats.size() - 1 - i; j++) {
                if (timePerCatFiltered.get(allCats.get(j)) < timePerCatFiltered.get(allCats.get(j + 1))) {
                    String temp = allCats.get(j);
                    allCats.set(j, allCats.get(j + 1));
                    allCats.set(j + 1, temp);
                }
            }
        }

        int limit = Math.min(5, allCats.size());
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList();

        for (int i = 0; i < limit; i++) {
            String cat = allCats.get(i);
            double mins = timePerCatFiltered.get(cat);
            String timeLabel = mins < 1.0
                    ? (int) Math.round(mins * 60) + " sec"
                    : (int) mins + " min";
            slices.add(new PieChart.Data(cat + " (" + timeLabel + ")", mins));
        }

        timePerCategoryChart.setData(slices);
        timePerCategoryChart.setTitle("");
        timePerCategoryChart.setLabelsVisible(false);
        timePerCategoryChart.setLegendVisible(true);
    }

    private void loadDailyLineChart() {
        if (timePerDayChart == null) return;

        Map<String, Double> timePerDay = StatsManagement.getTimeSpentPerDay();
        if (timePerDay.isEmpty()) return;

        List<String> sortedDates = new ArrayList<>(timePerDay.keySet());
        Collections.sort(sortedDates);

        if (sortedDates.size() > selectedDays)
            sortedDates = sortedDates.subList(sortedDates.size() - selectedDays, sortedDates.size());

        timePerDayChart.getData().clear();

        javafx.scene.chart.CategoryAxis xAxis = (javafx.scene.chart.CategoryAxis) timePerDayChart.getXAxis();
        if (xAxis != null) {
            xAxis.getCategories().clear();
            xAxis.setCategories(FXCollections.observableArrayList(sortedDates));
            xAxis.setTickLabelRotation(45);
        }

        javafx.scene.chart.NumberAxis yAxis = (javafx.scene.chart.NumberAxis) timePerDayChart.getYAxis();
        double maxMinutes = sortedDates.stream().mapToDouble(timePerDay::get).max().orElse(0);

        if (yAxis != null) {
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(Math.max(maxMinutes * 1.1, 5));
            yAxis.setTickUnit(Math.max(1, Math.ceil(maxMinutes / 10)));
            yAxis.setAutoRanging(false);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Minutes");
        for (String date : sortedDates) {
            double displayValue = Math.round(timePerDay.get(date) * 100.0) / 100.0;
            series.getData().add(new XYChart.Data<>(date, displayValue));
        }
        timePerDayChart.getData().add(series);
        timePerDayChart.setLegendVisible(false);
    }

    private String formatTime(double minutes) {
        return minutes < 60
                ? String.format("%.0f min", minutes)
                : String.format("%.1f hrs", minutes / 60.0);
    }

    @FXML public void onTimeRange7Days()  { selectedDays = 7;  refreshCharts(); }
    @FXML public void onTimeRange14Days() { selectedDays = 14; refreshCharts(); }
    @FXML public void onTimeRange30Days() { selectedDays = 30; refreshCharts(); }

    private void refreshCharts() {
        loadCategoryPieChart();
        loadDailyLineChart();
        updateButtonStyles();
    }

    // Swap CSS classes instead of calling setStyle() ──────────────────────
    private void updateButtonStyles() {
        if (btn7Days == null || btn14Days == null || btn30Days == null) return;

        setRangeBtn(btn7Days,  selectedDays == 7);
        setRangeBtn(btn14Days, selectedDays == 14);
        setRangeBtn(btn30Days, selectedDays == 30);
    }

    private void setRangeBtn(Button btn, boolean active) {
        btn.getStyleClass().removeAll("stats-range-btn", "stats-range-btn-active");
        btn.getStyleClass().add(active ? "stats-range-btn-active" : "stats-range-btn");
    }

    private void loadTodayProgress() {
        String today = java.time.LocalDate.now().toString();
        Map<String, Double> timePerDay = StatsManagement.getTimeSpentPerDay();
        double todayMinutes = timePerDay.getOrDefault(today, 0.0);
        int dailyTarget = Controller.getDailyTarget();
        int progressPercent = Math.min(100, (int) ((todayMinutes / dailyTarget) * 100));

        if (todayTimeLabel != null) {
            todayTimeLabel.setText(todayMinutes < 1.0
                    ? (int) Math.round(todayMinutes * 60) + " sec"
                    : (int) todayMinutes + " min");
        }

        if (targetLabel != null)
            targetLabel.setText(dailyTarget + " min");

        // Swap CSS class instead of inline setStyle() ──────────────────────
        if (progressStatusLabel != null) {
            progressStatusLabel.setText(progressPercent + "% Complete");
            progressStatusLabel.getStyleClass()
                    .removeAll("progress-status-good", "progress-status-mid", "progress-status-low");
            if (progressPercent >= 100)
                progressStatusLabel.getStyleClass().add("progress-status-good");
            else if (progressPercent >= 50)
                progressStatusLabel.getStyleClass().add("progress-status-mid");
            else
                progressStatusLabel.getStyleClass().add("progress-status-low");
        }
    }

    @FXML public void onBackButtonClick() {}
}