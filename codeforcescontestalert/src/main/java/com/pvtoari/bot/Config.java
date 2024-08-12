package com.pvtoari.bot;

import java.util.*;
import java.io.*;

public class Config {

    public static Boolean debug = true;
    public static String BOT_TOKEN = null;
    public static String CODEFORCES_API = null;
    public static Long API_REQUEST_FREQUENCY = null; // in hours in config file but here is in millis for easier calculations
    public static String DEFAULT_MSG = null;
    public static String HELP_MSG = null;
    public static String UNKNOWN_COMMAND_MSG = null;

    private static String[] parseFieldsFromPropertiesFile() {
        String[] res = new String[7];
        Scanner fr = null;

        try {
            fr = new Scanner(new File("files/config.properties"));
            for(int i = 0; fr.hasNext();) {
                String line = fr.nextLine();

                if(line.startsWith("#")) continue;
                res[i] = line.substring(line.indexOf("=") + 1).trim();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            fr.close();
        }

        return res;
    }

    public static void loadAndSetFields() {
        String[] fields = parseFieldsFromPropertiesFile();

        debug = Boolean.parseBoolean(fields[0]);
        BOT_TOKEN = fields[1];
        CODEFORCES_API = fields[2];
        API_REQUEST_FREQUENCY = Long.parseLong(fields[3])*36000000; // converting hours to millis
        DEFAULT_MSG = fields[4];
        HELP_MSG = fields[5].replace("\\n", "\n");
        UNKNOWN_COMMAND_MSG = fields[6];
    }
}
