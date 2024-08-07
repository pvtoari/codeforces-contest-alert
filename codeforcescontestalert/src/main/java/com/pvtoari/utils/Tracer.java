package com.pvtoari.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.pvtoari.bot.Config;

public class Tracer {
    public static final int INFO = 0;
    public static final int LOW_RISK = 1;
    public static final int MEDIUM_RISK = 2;
    public static final int HIGH_RISK = 3;

    public static void log(int level, String message) {
        if(Config.debug == false) return;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        String date = sdf.format(cal.getTime());

        String res = "(" + date + ") ";
        
        switch (level) {
            case INFO:
                res += "[INFO] " + message;
                break;
            case LOW_RISK:
                res += "[LOW RISK] " + message;
                break;
            case MEDIUM_RISK:
                res += "[MEDIUM RISK] " + message;
                break;
            case HIGH_RISK:
                res += "[HIGH RISK] " + message;
            break;
        }

        System.out.println(res);

        try {
            logToFile(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void logToFile(String message) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String date = sdf.format(now);

        File file = new File("log-" + date + ".txt");
        if(!file.exists()) {
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file, true);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(message);

        pw.close();
    }
}
