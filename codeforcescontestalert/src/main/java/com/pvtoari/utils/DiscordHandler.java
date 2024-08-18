package com.pvtoari.utils;

import com.pvtoari.bot.Config;
import java.awt.Color;

public class DiscordHandler {
    public static DiscordWebhook botWebhook = new DiscordWebhook(Config.DISCORD_WEBHOOK_URL);

    public static void sendFormattedContests() {
        if(!Config.DISCORD_WEBHOOK_ENABLED) return;

        String content = Requests.getFormatedUpcomingContests().replace("\n", "\\n");
        sendDefaultEmbeddedMessage("Upcoming contests", content, Color.RED);
    }

    public static void sendPlainText(String message) {
        if(!Config.DISCORD_WEBHOOK_ENABLED) return;

        message = message.replace("\n", "\\n");
        botWebhook.setAvatarUrl(Config.DISCORD_WEBHOOK_AVATAR);
        botWebhook.setUsername(Config.DISCORD_WEBHOOK_NAME);
        botWebhook.setContent(message);

        try {
            botWebhook.execute();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendAlert(Contest contest) {
        if(!Config.DISCORD_WEBHOOK_ENABLED) return;

        String content = contest.getFormattedMessageContent().replace("\n", "\\n");
        sendDefaultEmbeddedMessage("New contest listed!", content, Color.RED);
    }

    public static void sendDefaultEmbeddedMessage(String title, String description, Color color) {
        if(!Config.DISCORD_WEBHOOK_ENABLED) return;

        botWebhook.setAvatarUrl(Config.DISCORD_WEBHOOK_AVATAR);
        botWebhook.setUsername(Config.DISCORD_WEBHOOK_NAME);
        botWebhook.addEmbed(new DiscordWebhook.EmbedObject().setTitle(title).setColor(color).setDescription(description));

        try {
            botWebhook.execute();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
