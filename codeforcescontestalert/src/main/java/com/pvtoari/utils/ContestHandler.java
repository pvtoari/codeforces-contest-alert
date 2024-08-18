package com.pvtoari.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import com.pvtoari.bot.Config;

public class ContestHandler {

    public String currentRawContent = "";
    public Thread thread;
    public Contest[] currentContests;
    
    /*
     * This class will be responsible for handling correctly contest data and requests to the Codeforces API
     * since its not correct to always request the API for the same data, this class will be responsible for
     * storing the data and updating it when necessary.
     * 
     * Therefore the classes that ask for data will ask this class for it and get methods from another classes will be implemented here
     */

    // DISCLAIMER: this implementation is not the desired one, it's just a placeholder for the final implementation
    // because my intention is to use a thread that gets the content every 12h but i realized that i wanted to do that
    // when i finished implementing this system XD so i'll leave it like this for now
    // if u wonder why, the main problem is that the content is updated when a user requests it, so if i intend to make 
    // an alert system or something like that, the content will be updated every time a user requests it, which is not
    // the desired behavior, so i'll leave it like this for now and implement the thread later

    // update: i implemented the thread, but i'll leave the old code here because it can be useful in some cases

    public void startHandling() {
        String pastContent = currentRawContent;
        if(pastContent.isBlank()) pastContent = getRawFilteredContentv2();

        Contest[] pastContests = Contest.parseRawFilteredData(pastContent);
        this.currentRawContent = getRawFilteredContentv2();
        this.currentContests = Contest.parseRawFilteredData(currentRawContent);

        final String[] pastContentHolder = new String[1];
        final Contest[][] pastContestsHolder = new Contest[1][];
        pastContentHolder[0] = pastContent;
        pastContestsHolder[0] = pastContests;

        checkForNewContests(pastContests, currentContests);
        Thread t = new Thread(() -> {
            while(true) {
               // if im correct, the method will only check for outdated or corrupt data the first time it's called
               // then it will only check if the data is outdated, im start to think that the method could be improved

                try {
                    Thread.sleep(Config.API_REQUEST_FREQUENCY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                pastContentHolder[0] = currentRawContent;
                pastContestsHolder[0] = null;
    
                Tracer.log(Tracer.INFO, "Contest handler is trying to update raw content.");
                currentRawContent = getRawFilteredContentv2();
                pastContestsHolder[0] = Contest.parseRawFilteredData(pastContentHolder[0]);

                if(currentContests!= null) {
                    pastContestsHolder[0] = Arrays.copyOf(currentContests, currentContests.length);
                } else {
                    Tracer.log(Tracer.HIGH_RISK, "Current contests are null, first time running? Actually this shouldn't run never oopsie");
                }
                this.currentContests = Contest.parseRawFilteredData(currentRawContent);

                checkForNewContests(pastContests, currentContests);
            }
        });

        this.thread = t;
        t.start();
    }

    public void stopHandling() {
        thread.interrupt();
    }

    // private static ArrayList<Contest> lookForNewContests(Contest[] older, Contest[] newer) {
    //     ArrayList<Contest> res = new ArrayList<Contest>();

    //     for(int i = 0; i < newer.length; i++) {
    //         for(int j = 0; j < older.length; j++) {
    //             if(newer[i].getId() == older[j].getId()) {
    //                 res.add(newer[i]);
    //             }
    //         }
    //     }

    //     return res;
    // }

    private static void checkForNewContests(Contest[] older, Contest[] newer) {
        if(!Contest.arraysEqual(older, newer)) {
            Tracer.log(Tracer.INFO, "New contests found, sending alerts...");
            ArrayList<Contest> newContests = Contest.arraysDiff(older, newer);

            for(Contest c : newContests) {
                DiscordHandler.sendAlert(c);
            }

        } else {
            Tracer.log(Tracer.INFO, "No new contests found.");
        }
    }

    private static String getRawFilteredContentv2() {
        // since the content is updated every time a user requests it, we don't need to check if it's outdated
        // but ill leave the code here because it can be helpful in some cases maybe idk

        Tracer.log(Tracer.INFO, "Determining source of raw content...");
        String res = "";
        File rawFile = new File("files/rawContent.txt");
        
        if(!rawFile.exists()) {
            Tracer.log(Tracer.INFO, "Raw content file not found, requesting from API and saving...");
            res = Requests.getRawFilteredContent();
            saveRawContent(res, rawFile);
        } else {
            Tracer.log(Tracer.INFO, "Raw content file found, checking if corrupt or outdated...");

            if(isContentCorrupted(rawFile)) {
                Tracer.log(Tracer.INFO, "Raw content is corrupted, requesting from API...");
                res = Requests.getRawFilteredContent();

                if(res.equals("fail")) Tracer.log(Tracer.MEDIUM_RISK, "API request failed, content will remain corrupted, try again later...");
                else {
                    Tracer.log(Tracer.INFO, "API request successful, saving new content...");
                    saveRawContent(res, rawFile); // if not failed, save the content

                }
                
            } else if(isContentOutdated(rawFile)) {
                Tracer.log(Tracer.INFO, "Raw content is outdated, requesting from API...");
                res = Requests.getRawFilteredContent();

                if(res.equals("fail")) {
                    Tracer.log(Tracer.MEDIUM_RISK, "API request failed, outdated content will be used...");
                    res = loadRawContent(rawFile);
                    // if the request fails, the content will be the same as the last one, so we don't need to save it again
                } else {
                    Tracer.log(Tracer.INFO, "API request successful, saving new content...");
                    saveRawContent(res, rawFile); // if not failed, save the content
                }
            } else {
                Tracer.log(Tracer.INFO, "Raw content is not corrupted and up to date, loading from file...");
                res = loadRawContent(rawFile);
            }

        }

        
        return res;
    }

    private static String[] loadRawFile(File rawFile) {
        // if everything is correct, the first line will be the date of the last update and the second line will be the content
        String res[] = {"", ""};
        Scanner sc = null;
        try {
            sc = new Scanner(rawFile);
            for(int i = 0; sc.hasNext(); i++) {
                res[i] = sc.nextLine();
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }

        return res;
    }

    private static String loadRawContent(File rawFile) {
        String[] lines = loadRawFile(rawFile);

        if(lines.length == 1) return "fail"; // if there's no content, we will provoke the 'chain reaction' from throwing "fail" and magic will happen
        else return lines[1];
    }

    private static boolean isContentOutdated(File rawFile) {
        boolean res = true; // assuming the file is outdated so if scanner fails or if there's wrong data it will retrieve data from api
        try {
            String[] lines = loadRawFile(rawFile);

            if(lines[0].contains("datemillis:")) {
                String millis = lines[0].substring(lines[0].indexOf(":") + 1);
                long lastUpdate = Long.parseLong(millis);
                long now = new Date().getTime();

                Tracer.log(Tracer.INFO, "Last update: " + lastUpdate + " Current time: " + now + " Difference: " + (now - lastUpdate) + "ms / " + String.format(Locale.ENGLISH, "%.4f", (now - lastUpdate)/60000f) + "min / " +String.format(Locale.ENGLISH,"%.4f",(now - lastUpdate)/3600000f) + "h / ");
                if((now - lastUpdate) < Config.API_REQUEST_FREQUENCY) {
                    res = false;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    private static boolean isContentCorrupted(File rawFile) {
        String[] lines = loadRawFile(rawFile);

        return lines[1].isBlank() || lines[1].equals("fail");
    }


    private static void saveRawContent(String content, File rawFile) {
        FileWriter fw = null;
        try {
            rawFile.createNewFile();
            
            fw = new FileWriter(rawFile);
            fw.write("datemillis:" + new Date().getTime() + "\n");
            fw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
