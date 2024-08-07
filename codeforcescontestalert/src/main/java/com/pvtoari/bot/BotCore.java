package com.pvtoari.bot;

import java.util.ArrayList;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.pvtoari.utils.Requests;
import com.pvtoari.utils.Tracer;

public class BotCore implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient client = new OkHttpTelegramClient(Config.BOT_TOKEN);
    private boolean awaitingRawContent = false;
    
    @Override
    public void consume(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String user = getUser(update);

            Tracer.log(Tracer.INFO, "Message received from " + user + ": " + messageText);

            if(!awaitingRawContent) {
                executeCommand(messageText, update, chat_id);
                Tracer.log(Tracer.LOW_RISK, user + " attempting to perform " + "\"messageText\"" + " command.");
            }else {
                Tracer.log(Tracer.MEDIUM_RISK, "Awaiting raw content confirmation from " + user);
                handleRawYesNo(update, chat_id, messageText);
            }
            
        }
    }

    private void executeCommand(String command, Update update, long chat_id) {
        String user = getUser(update);
        switch(command) {
            case "/help":
                Tracer.log(Tracer.INFO, "User " + user + " requested help message.");
                sendPlainText(update, chat_id, Config.HELP_MSG);
                break;
            case "/start":
                Tracer.log(Tracer.LOW_RISK, "User " + user + " started the bot.");
                sendPlainText(update, chat_id, Config.DEFAULT_MSG);
                break;
            case "/raw":
                Tracer.log(Tracer.MEDIUM_RISK, "User " + user + " requested raw content.");
                sendPlainText(update, chat_id, "Warning: This command sends the raw content of the Codeforces API. \n\nIt may be too long to be displayed in a single message and could cause performance troubles. Do you want to continue? (yes/no). \n\nConfirm before five seconds.");
                awaitingRawContent = true;
                break;
            case "/filteredRaw":
                Tracer.log(Tracer.LOW_RISK, "User " + user + " requested filtered raw content.");
                sendRawFilteredContent(update, chat_id);
                break;
            case "/upcoming":
                Tracer.log(Tracer.LOW_RISK, "User " + user + " requested upcoming contests.");
                sendPlainText(update, chat_id, Requests.getFormatedUpcomingContests());
                break;
            default:
                Tracer.log(Tracer.LOW_RISK, "User " + user + " performed an unknown command.");
                sendPlainText(update, chat_id, Config.UNKNOWN_COMMAND_MSG);
            break;
        }
    }

    private void sendRawFilteredContent(Update update, long chat_id) {
        String user = getUser(update);

        Tracer.log(Tracer.INFO, "Sending filtered raw content to user " + user);
        sendPlainText(update, chat_id, Requests.getRawFilteredContent());
    }

    private void sendFullRawContent(Update update, long chat_id) {
        String user = getUser(update);
        Tracer.log(Tracer.HIGH_RISK, "Splitted raw content is being sent to " + user + "... API may be overloaded and data delivery may be delayed.");
        int i = 0;
        ArrayList<String> messages = Requests.getSplittedRawCodeforcesContests();
            for(String messageText : messages) {
                i++;
                SendMessage message = SendMessage
                .builder()
                .chatId(chat_id)
                .text(messageText)
                .build();
                
                try {
                    client.execute(message);
                    Tracer.log(Tracer.HIGH_RISK, "Sending message " + i + " of " + messages.size() + " to " + user);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    Tracer.log(Tracer.HIGH_RISK, "Failure while sending message " + i + " of " + messages.size() + " to " + user);
                }
            }

        Tracer.log(Tracer.HIGH_RISK, "Finished sending raw content to " + user);
    }

    private void sendPlainText(Update update, long chat_id, String messageText) {
        String user = getUser(update);
        SendMessage message = SendMessage
            .builder()
            .chatId(chat_id)
            .text(messageText)
            .build();

        try {
            client.execute(message);
            Tracer.log(Tracer.LOW_RISK, "Sending plain text message to " + user);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            Tracer.log(Tracer.HIGH_RISK, "Failure while sending plain text message to " + user);
        }
    }

    private void handleRawYesNo(Update update, long chat_id, String messageText) {
        String user = getUser(update);
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
                Tracer.log(Tracer.HIGH_RISK, "User " + user + "confirmed raw content sending.");
                sendFullRawContent(update, chat_id);
            } else {
                sendPlainText(update, chat_id, "Command cancelled.");
                Tracer.log(Tracer.INFO, "Raw content sending cancelled by " + user);
            }
        }
    }

    private static String getUser(Update update) {
        String res = update.getMessage().getFrom().getUserName();

        if(res == null) {
            res = "";
            res += "'" + update.getMessage().getFrom().getFirstName();
            res += " " + update.getMessage().getFrom().getLastName() + "'";
        } else {
            res = "'" + res + "'";
        }

        return res;
    }
}
