package com.pvtoari;

import java.util.Scanner;

import org.telegram.telegrambots.longpolling.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.pvtoari.bot.*;
import com.pvtoari.utils.*;

public class Main {

    public static ContestHandler globalContestHandler;

    public static void main(String[] args) {
        Tracer.log(Tracer.INFO, "Attempting to start the bot...");

        BotSetup setup = new BotSetup();
        setup.start();

        try (TelegramBotsLongPollingApplication botApp = new TelegramBotsLongPollingApplication()) {
            botApp.registerBot(Config.BOT_TOKEN, new BotCore());

            globalContestHandler = new ContestHandler();
            globalContestHandler.startHandling(); // start the content handling thread

            Tracer.log(Tracer.INFO, "Bot started successfully.");

            new Thread(() -> {
                Scanner kbd = new Scanner(System.in);
                while(kbd.hasNextLine()) {
                    String line = kbd.nextLine();
                    if(line.equals("stop")) {
                        Tracer.log(Tracer.INFO, "Stopping the bot...");
                        globalContestHandler.stopHandling(); // stop the content handling thread
                        try {
                            botApp.stop();
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if(line.equals("debug on")) {
                        Config.debug = true;
                        Tracer.log(Tracer.INFO, "Debug mode enabled.");
                    } else if(line.equals("debug off")) {
                        Config.debug = false;
                        Tracer.log(Tracer.INFO, "Debug mode disabled.");
                    }
                }

                kbd.close();
            }).start();

            Thread.currentThread().join();
        } catch (Exception e) {
            Tracer.log(Tracer.HIGH_RISK, "Bot failed to start, check your internet connection and/or the bot token.");
            globalContestHandler.stopHandling(); // stop the content handling thread
            e.printStackTrace();
        }
    }
 }