package com.pvtoari.bot;

import java.util.ArrayList;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.pvtoari.utils.Requests;

public class BotCore implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient client = new OkHttpTelegramClient(Config.BOT_TOKEN);
    private boolean awaitingRawContent = false;
    
    @Override
    public void consume(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            
            if(!awaitingRawContent) executeCommand(messageText, update, chat_id);
            else handleRawYesNo(update, chat_id, messageText);
            
        }
    }

    private void executeCommand(String command, Update update, long chat_id) {
        switch(command) {
            case "/help":
                sendPlainText(update, chat_id, Config.HELP_MSG);
                break;
            case "/start":
                sendPlainText(update, chat_id, Config.DEFAULT_MSG);
                break;
            case "/raw":
                sendPlainText(update, chat_id, "Warning: This command sends the raw content of the Codeforces API. \n\nIt may be too long to be displayed in a single message and could cause performance troubles. Do you want to continue? (yes/no). \n\nConfirm before five seconds.");
                awaitingRawContent = true;
                break;
            case "/filteredRaw":
                sendRawFilteredContent(update, chat_id);
                break;
            default:
                sendPlainText(update, chat_id, Config.UNKNOWN_COMMAND_MSG);
        }
    }

    private void sendRawFilteredContent(Update update, long chat_id) {
        String messageContent = Requests.getRawCodeforcesContests();
        char[] contentChars = messageContent.toCharArray();
        int stoppingIndex = messageContent.indexOf("\"phase\":\"FINISHED\"");
        int deletingCurlyBraceIndex = stoppingIndex;
            
        while(contentChars[deletingCurlyBraceIndex] != '{') {
            contentChars[deletingCurlyBraceIndex] = 0;
            deletingCurlyBraceIndex--;
        }
            
        contentChars[deletingCurlyBraceIndex] = 0;
        contentChars[deletingCurlyBraceIndex-1] = 0;
            
        int beautifyIndex = messageContent.indexOf("[");
        for(int i = 0; i <= beautifyIndex; i++) {
            contentChars[i] = 0;
        }
            
        String newContent = new String(contentChars).substring(0, stoppingIndex);
        sendPlainText(update, chat_id, newContent);
    }

    private void sendFullRawContent(Update update, long chat_id) {
        ArrayList<String> messages = Requests.getSplittedRawCodeforcesContests();
            for(String messageText : messages) {
                SendMessage message = SendMessage
                    .builder()
                    .chatId(chat_id)
                    .text(messageText)
                    .build();

                try {
                    client.execute(message);
                    System.out.println("[Debug] Message sent to " + update.getMessage().getFrom().getUserName());
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
    }

    private void sendPlainText(Update update, long chat_id, String messageText) {
        SendMessage message = SendMessage
            .builder()
            .chatId(chat_id)
            .text(messageText)
            .build();

        try {
            client.execute(message);
            System.out.println("[Debug] Message sent to " + update.getMessage().getFrom().getUserName());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleRawYesNo(Update update, long chat_id, String messageText) {
        if(!awaitingRawContent) {
            return;
        } else {
            awaitingRawContent = false;

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(messageText.toLowerCase().equals("yes")) {
                sendFullRawContent(update, chat_id);
            } else {
                sendPlainText(update, chat_id, "Command cancelled.");
            }
        }
    }
}
