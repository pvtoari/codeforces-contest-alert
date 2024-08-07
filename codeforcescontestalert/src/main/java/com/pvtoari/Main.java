package com.pvtoari;

import org.telegram.telegrambots.longpolling.*;

import com.pvtoari.bot.BotCore;
import com.pvtoari.bot.Config;
import com.pvtoari.utils.Tracer;

public class Main {
    public static void main(String[] args) {
        Tracer.log(Tracer.INFO, "Attempting to start the bot...");

        try (TelegramBotsLongPollingApplication botApp = new TelegramBotsLongPollingApplication()) {
            botApp.registerBot(Config.BOT_TOKEN, new BotCore());

            Tracer.log(Tracer.INFO, "Bot started successfully.");
            Thread.currentThread().join();
        } catch (Exception e) {
            Tracer.log(Tracer.HIGH_RISK, "Bot failed to start, check your internet connection and/or the bot token.");
            e.printStackTrace();
        }
    }
 }