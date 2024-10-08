package com.pvtoari.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;

import com.pvtoari.bot.Config;

public class ContestHandler {
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

    public static String getRawFilteredContentv2() {
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
