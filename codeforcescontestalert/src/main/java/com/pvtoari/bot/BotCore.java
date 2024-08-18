package com.pvtoari.bot;

import java.util.ArrayList;
import java.util.Date;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import com.pvtoari.Main;
import com.pvtoari.utils.Requests;
import com.pvtoari.utils.Tracer;

public class BotCore implements LongPollingSingleThreadUpdateConsumer {
    private TelegramClient client = new OkHttpTelegramClient(Config.BOT_TOKEN);
    private boolean awaitingRawContent = false;
    
    @Override
    public void consume(Update update) {
        
        
        if(update.hasMessage() && update.getMessage().hasText() && tryIgnoreByTime(update)) {

            long chat_id = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String user = getUser(update);

            Tracer.log(Tracer.INFO, "Message received from " + user + ": " + messageText, update);

            if(!awaitingRawContent) {
                executeCommand(messageText, update, chat_id);
                Tracer.log(Tracer.LOW_RISK, user + " attempting to perform " + "\"messageText\"" + " command.", update);
            }else {
                Tracer.log(Tracer.MEDIUM_RISK, "Awaiting raw content confirmation from " + user, update);
                handleRawYesNo(update, chat_id, messageText);
            }
            
        }
    }

    private void executeCommand(String command, Update update, long chat_id) {
        String user = getUser(update);
        switch(command) {
            case "/help":
                Tracer.log(Tracer.INFO, "User " + user + " requested help message.", update);
                sendPlainText(update, chat_id, Config.HELP_MSG);
                break;
            case "/start":
                Tracer.log(Tracer.LOW_RISK, "User " + user + " started the bot.", update);
                sendPlainText(update, chat_id, Config.DEFAULT_MSG);
                break;
            case "/raw":
                Tracer.log(Tracer.MEDIUM_RISK, "User " + user + " requested raw content.");
                sendPlainText(update, chat_id, "Warning: This command sends the raw content of the Codeforces API. \n\nIt may be too long to be displayed in a single message and could cause performance troubles. Do you want to continue? (yes/no). \n\nConfirm before five seconds.");
                awaitingRawContent = true;
                break;
            case "/filteredRaw":
                Tracer.log(Tracer.LOW_RISK, "User " + user + " requested filtered raw content.", update);
                sendRawFilteredContent(update, chat_id);
                break;
            case "/upcoming":
                Tracer.log(Tracer.LOW_RISK, "User " + user + " requested upcoming contests.", update);
                sendPlainText(update, chat_id, Requests.getFormatedUpcomingContests());
                break;
            default:
                Tracer.log(Tracer.LOW_RISK, "User " + user + " performed an unknown command.", update);
                sendPlainText(update, chat_id, Config.UNKNOWN_COMMAND_MSG);
            break;
        }
    }

    private void sendRawFilteredContent(Update update, long chat_id) {
        String user = getUser(update);

        Tracer.log(Tracer.INFO, "Sending filtered raw content to user " + user, update);
        //sendPlainText(update, chat_id, Requests.getRawFilteredContent()); this line is replaced by the new handler
        //sendPlainText(update, chat_id, ContestHandler.getRawFilteredContentv2());
        sendPlainText(update, chat_id, Main.globalContestHandler.currentRawContent);
    }

    private void sendFullRawContent(Update update, long chat_id) {
        String user = getUser(update);
        Tracer.log(Tracer.HIGH_RISK, "Splitted raw content is being sent to " + user + "... API may be overloaded and data delivery may be delayed.", update);
        int i = 0;
        ArrayList<String> messages = Requests.getSplittedRawCodeforcesContests();
        if(messages == null) {
            Tracer.log(Tracer.HIGH_RISK, "Raw content splitting failed. Aborting raw content sending.", update);
            sendPlainText(update, chat_id, "An error ocurred while processing your request. Please try again later.");
        }

            for(String messageText : messages) {
                i++;
                SendMessage message = SendMessage
                .builder()
                .chatId(chat_id)
                .text(messageText)
                .build();
                
                try {
                    client.execute(message);
                    Tracer.log(Tracer.HIGH_RISK, "Sending message " + i + " of " + messages.size() + " to " + user, update);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                    Tracer.log(Tracer.HIGH_RISK, "Failure while sending message " + i + " of " + messages.size() + " to " + user, update);
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

        if(messageText.isBlank()) {
            Tracer.log(Tracer.HIGH_RISK, String.format("Cannot send empty or null message to %s, aborting",user), update);
            return;
        }

        try {
            client.execute(message);
            Tracer.log(Tracer.LOW_RISK, "Sending plain text message to " + user, update);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            Tracer.log(Tracer.HIGH_RISK, "Failure while sending plain text message to " + user, update);
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
                Tracer.log(Tracer.HIGH_RISK, "User " + user + "confirmed raw content sending.", update);
                sendFullRawContent(update, chat_id);
            } else {
                sendPlainText(update, chat_id, "Command cancelled.");
                Tracer.log(Tracer.INFO, "Raw content sending cancelled by " + user, update);
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

    private boolean tryIgnoreByTime(Update update) {
        Date date = new Date((long)update.getMessage().getDate() * 1000);
        if(new Date().getTime() - date.getTime() > 300000) {
            Tracer.log(Tracer.LOW_RISK, "Ignoring message from " + getUser(update) + " due to time.", update);
            return false;
        } else {
            Tracer.log(Tracer.INFO, "Message from " + getUser(update) + " is not being ignored.", update);
            return true;
        }
    }
}
