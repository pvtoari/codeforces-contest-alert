package com.pvtoari.bot;

import java.util.*;
import java.io.*;

public class Config {

    public static Boolean debug = true;
    public static String BOT_TOKEN = null;
    public static String CODEFORCES_API = null;
    public static String DEFAULT_MSG = null;
    public static String HELP_MSG = null;
    public static String UNKNOWN_COMMAND_MSG = null;

    private static String[] parseFieldsFromPropertiesFile() {
        String[] res = new String[6];

        try (Scanner fr = new Scanner(new File("files/config.properties"))) {
            for(int i = 0; fr.hasNext();) {
                String line = fr.nextLine();

                if(line.startsWith("#")) continue;
                res[i] = line.substring(line.indexOf("=") + 1).trim();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }

    public static void loadAndSetFields() {
        String[] fields = parseFieldsFromPropertiesFile();

        debug = Boolean.parseBoolean(fields[0]);
        BOT_TOKEN = fields[1];
        CODEFORCES_API = fields[2];
        DEFAULT_MSG = fields[3];
        HELP_MSG = fields[4].replace("\\n", "\n");
        UNKNOWN_COMMAND_MSG = fields[5];
    }
}
