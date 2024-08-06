package com.pvtoari;

import org.telegram.telegrambots.longpolling.*;

import com.pvtoari.bot.BotCore;
import com.pvtoari.bot.Config;

public class Main {
    public static void main(String[] args) {
        try (TelegramBotsLongPollingApplication botApp = new TelegramBotsLongPollingApplication()) {
            botApp.registerBot(Config.BOT_TOKEN, new BotCore());
            System.out.println("Bot started");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\nBot failed to start");
        }
    }
 }