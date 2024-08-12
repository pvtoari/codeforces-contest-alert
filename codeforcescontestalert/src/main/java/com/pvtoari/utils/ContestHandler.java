package com.pvtoari.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import com.pvtoari.bot.Config;

public class ContestHandler {
    /*
     * TODO: This class will be responsible for handling correctly contest data and requests to the Codeforces API
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
            Tracer.log(Tracer.INFO, "Raw content file found, checking if outdated...");
            if(isContentOutdated(rawFile)) {
                Tracer.log(Tracer.INFO, "Raw content is outdated, requesting from API and saving...");
                res = Requests.getRawFilteredContent();
                saveRawContent(res, rawFile);
            } else {
                Tracer.log(Tracer.INFO, "Raw content is up to date, loading from file...");
                res = loadRawContent(rawFile);
            }

        }

        return res;
    }

    private static String loadRawContent(File rawFile) {
        String res = "";
        Scanner sc = null;
        try {
            sc = new Scanner(rawFile);
            while(sc.hasNext()) {
                res += sc.nextLine() + "\n";
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }

        return res;
    }

    private static boolean isContentOutdated(File rawFile) {
        boolean res = true; // assuming the file is outdated so if scanner fails or if there's wrong data it will retrieve data from api
        Scanner sc = null;
        try {
            sc = new Scanner(rawFile);
            String firstLine = "";

            if(sc.hasNext()) {
                firstLine = sc.nextLine();
            }

            if(firstLine.contains("datemillis:")) {
                String millis = firstLine.substring(firstLine.indexOf(":") + 1);
                long lastUpdate = Long.parseLong(millis);
                long now = new Date().getTime();

                if((now - lastUpdate) < Config.API_REQUEST_FREQUENCY) {
                    res = false;
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            sc.close();
        }

        return res;
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
