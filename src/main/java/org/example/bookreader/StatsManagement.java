package org.example.bookreader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDate;
import java.util.*;
public class StatsManagement {
    private static final String Stats_file_name="stats.json";
    private static final ObjectMapper mapper=new ObjectMapper();
    //record pages read for one day
    public static void readLog(String title, int pages, long seconds, String category) {
        String today = LocalDate.now().toString();
        SingleReadingEvent event = new SingleReadingEvent(today, title, pages, seconds, category);
        saveNewEvent(event);
    }
    private static List<SingleReadingEvent> loadHistory(){
        File file=new File(Stats_file_name);
        if(!file.exists()){
            return new ArrayList<>();
        }
        try{
            return mapper.readValue(file, new TypeReference<List<SingleReadingEvent>>() {});
        } catch(Exception e){
            return new ArrayList<>();
        }
    }
    public static void saveNewEvent(SingleReadingEvent newEvent){
        List<SingleReadingEvent> history=loadHistory();
        history.add(newEvent);
        try{
            mapper.writeValue(new File(Stats_file_name),history);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static Map<String,Double> getTimeSpentPerDay(){
        List<SingleReadingEvent> events=loadHistory();
        Map<String,Double> timePerDay=new HashMap<>();
        for(SingleReadingEvent event : events){
            String date = event.getDate();
            double minutes = event.getSecondsRead()/60.0;
            if(timePerDay.containsKey(date)){
                //already started a total for this date
                double oldTotal=timePerDay.get(date);
                timePerDay.put(date,minutes+oldTotal);
            }
            else{
                //this is the first time this date has been seen
                timePerDay.put(date,minutes);
            }
        }
        return timePerDay;
    }
    public static Map<String,Double> getTimePerCategory(){
        List<SingleReadingEvent> events=loadHistory();
        Map<String,Double>timePerCategory=new HashMap<>();
        for(SingleReadingEvent event:events){
            String category=event.getCategory();
            double minutes=event.getSecondsRead()/60.0;
            if(timePerCategory.containsKey(category)){
                double oldTotal=timePerCategory.get(category);
                timePerCategory.put(category,oldTotal+minutes);
            }
            else{
                timePerCategory.put(category,minutes);
            }
        }
        return timePerCategory;
    }
}
