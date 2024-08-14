package com.pvtoari.bot;

import java.util.*;
import java.io.*;

public class Config {

    public static Boolean debug=false;
    public static String BOT_TOKEN;
    public static String CODEFORCES_API;
    // -------------------------------------- //
    public static Long API_REQUEST_FREQUENCY; // in hours in config file but here is in millis for easier calculations
    // -------------------------------------- //
    public static String DEFAULT_MSG;
    public static String HELP_MSG;
    public static String UNKNOWN_COMMAND_MSG;
    // -------------------------------------- //
    public static boolean DISCORD_WEBHOOK_ENABLED;
    public static String DISCORD_WEBHOOK_URL;
    public static String DISCORD_WEBHOOK_NAME;
    public static String DISCORD_WEBHOOK_AVATAR;

    private static String[] parseFieldsFromPropertiesFile() {
        String[] res = new String[11];
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
        API_REQUEST_FREQUENCY = Long.parseLong(fields[3]); // converting hours to millis
        DEFAULT_MSG = fields[4];
        HELP_MSG = fields[5].replace("\\n", "\n");
        UNKNOWN_COMMAND_MSG = fields[6];
        DISCORD_WEBHOOK_ENABLED = Boolean.parseBoolean(fields[7]);
        DISCORD_WEBHOOK_URL = fields[8];
        DISCORD_WEBHOOK_NAME = fields[9];
        DISCORD_WEBHOOK_AVATAR = fields[10];
    }
}
