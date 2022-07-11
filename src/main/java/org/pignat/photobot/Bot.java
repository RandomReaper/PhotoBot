package org.pignat.photobot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Bot extends TelegramLongPollingBot {
    protected static final Logger log = LogManager.getLogger();
    protected String token;
    protected Set<Long> ids = new HashSet<Long>();
    protected Set<String> dirs = new HashSet<String>();

    public Bot(String t, Set<Long> i) {
        token = t;
        ids = i;

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendMsg("Bot is now online");
    }

    protected boolean sendMsg(Long id, String text) {
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

    protected boolean sendImg(Long id, Path p) {
        log.debug(String.format("sendImg(%s,%s)", id.toString(), p));
        try {
            SendPhoto sendPhotoRequest = new SendPhoto();
            sendPhotoRequest.setChatId(id.toString());
            sendPhotoRequest.setPhoto(new InputFile(p.toFile()));
            execute(sendPhotoRequest);
        } catch (TelegramApiException e) {
            log.error(e);
            return false;
        } catch (Exception e) {
            log.error(e);
            return false;
        }
        return true;
    }

    public void sendMsg(String text) {
        for (Long id : ids) {
            sendMsg(id, text);
        }
    }

    public void sendImg(Path p) {
        for (Long id : ids) {
            sendImg(id, p);
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
}

