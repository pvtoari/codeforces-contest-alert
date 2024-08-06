package com.pvtoari.bot;

public class Config {
    public boolean DEBUG = true;

    public static final String BOT_TOKEN = "";
    public static final String CODEFORCES_API = "https://codeforces.com/api/contest.list";
    public static final String DEFAULT_MSG = "Hello, I am a bot that sends you the list of upcoming Codeforces contests. Type /help to know more.";
    public static final String HELP_MSG = "List of commands: \n/help - Shows this message \n/raw - Sends the raw content of the Codeforces API \n/filteredRaw - Sends the filtered raw content of the Codeforces API \n/start - Starts the bot \n";
    public static final String UNKNOWN_COMMAND_MSG = "Unknown command. Type /help to know more.";
}
