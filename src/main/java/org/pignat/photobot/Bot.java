package org.pignat.photobot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.pignat.utils.Version;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class Bot extends TelegramLongPollingBot {
    protected static final Logger log = LogManager.getLogger();
    protected String token;
    protected Set<Long> ids = new HashSet<Long>();
    protected Set<String> dirs = new HashSet<String>();

    public Bot() {
        setup();
    }

    private void setup() {
        try (InputStream input = new FileInputStream("photobot.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            token = prop.getProperty("token");
            String sids = prop.getProperty("ids");
            if (sids != null) {
                ids.addAll(Arrays.stream(sids.split(",")).mapToLong(Long::parseLong).boxed().collect(Collectors.toSet()));
            }
            String sdirs = prop.getProperty("dirs");
            if (sdirs != null) {
                dirs.addAll(Arrays.asList(sdirs.split(",")));
            }
            log.debug("token:" + token);
            if (token == null || token.isBlank()) {
                log.error("no token");
            }
            log.info("ids:" + ids);
            if (ids.isEmpty()) {
                log.warn("ids is empty");
            }
            log.info("dirs:" + dirs);
            if (dirs.isEmpty()) {
                log.warn("dirs is empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendMsg("Bot is now online");
    }

    public void test() {
        log.info("hello");
    }

    public boolean sendMsg(Long id, String text) {
        log.debug(String.format("sendMsg(%s,%s)", id.toString(), text));
        SendMessage message = new SendMessage();
        message.setChatId(id.toString());
        message.setText(text);

        // HTML can be embedded in replies if required
        message.setParseMode(ParseMode.HTML);

        try {
            execute(message); // Call this method to send the message
        } catch (TelegramApiException e) {
            log.error(e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void sendMsg(String text) {
        for (Long id : ids) {
            sendMsg(id, text);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("onUpdateReceived:" + update);
        if (update.hasChannelPost()) {
            Long id = update.getChannelPost().getChatId();
            String text = update.getChannelPost().getText();
            if (!ids.contains(id)) {
                if (sendMsg(id, String.format("here is the channel id : " + id))) {
                    ids.add(id);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "holla";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public static String version() {
        return Version.string();
    }

    public static void main(String args[]) {
        Configurator.setRootLevel(Level.DEBUG);
        log.info("Starting photobot - " + version());
        Bot b = new Bot();
    }
}

