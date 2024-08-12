package com.pvtoari.bot;

import java.io.*;
import java.util.Scanner;

public class BotSetup {
    public void start() {
        System.out.println("Checking if the bot is set up...");
        if(!isSetup()) {
            System.out.println("Bot is not set up or some files are missing. Setting up the bot...");
            new File("files").mkdir();
            new File("files/logs").mkdir();
            try {
                new File("files/config.properties").createNewFile();
                FileWriter fw = new FileWriter("files/config.properties");
                fw.write("# This file contains the configuration of the bot. Fill in the fields below.\n");
                fw.write("debug=true\n");
                fw.write("BOT_TOKEN=\n");
                fw.write("CODEFORCES_API=https://codeforces.com/api/contest.list\n");
                fw.write("# in hours\n");
                fw.write("API_REQUEST_FREQUENCY=12\n");
                fw.write("# Messages, dont use quotes, Java scape characters are supported\n");
                fw.write("DEFAULT_MSG=Hello, I am a bot that sends you the list of upcoming Codeforces contests. Type /help to know more.\n");
                fw.write("HELP_MSG=List of commands: \\n/help - Shows this message \\n/upcoming Displays upcoming Codeforces contests \\n/raw - Sends the raw content of the Codeforces API \\n/filteredRaw - Sends the filtered raw content of the Codeforces API \\n/start - Starts the bot \n");
                fw.write("UNKNOWN_COMMAND_MSG=Unknown command. Type /help to know more.\n");
                fw.close();

                System.out.println("Bot set up successfully.");
                System.out.println("Please fill in the BOT_TOKEN in files/config.properties and restart the bot.");

                System.exit(0);
            } catch (IOException e) {
                System.out.println("Failed to set up the bot, check permissions and try again.\n");
                e.printStackTrace();}
        } else {
            //Config.loadAndSetFields();
            //System.out.println("Bot is already set up.");
            
            try {
                Config.loadAndSetFields();
            } catch (Exception e) {
                System.out.println("An error ocurred while trying to access config.properties file. There's a high probability that this is caused due to corrupt data, do you want to regenerate the config file? THE FILE WILL BE CLEARED (y/n)");
                Scanner sc = new Scanner(System.in);
                if(sc.nextLine().equals("y")) {
                    regenerateConfig();
                } else {
                    System.out.println("Check the config file and try again. Exiting...");
                    System.exit(0);
                }
                sc.close();
                regenerateConfig();
            }
            System.out.println("Bot is already set up.");
            
        }

    }

    public boolean isSetup() {
        return new File("files").exists() && new File("files/config.properties").exists() && new File("files/logs").exists();
    }

    public void regenerateConfig() {
        new File("files/config.properties").delete();
        start();
    }
}
