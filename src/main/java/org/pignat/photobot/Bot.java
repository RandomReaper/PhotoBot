package org.pignat.photobot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
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
    protected static String version = "ERROR:NO_VERSION";
    protected String token;
    protected Set<Long> ids = new HashSet<Long>();

    static {
        init();
    }

    /**
     * Get the version of the library
     *
     * @return A String containing the version
     */
    static public String version() {
        return version;
    }

    /**
     * Initialize the members
     */
    private static void init() {
        final String f = "/res/generated/version.txt";
        String v = null;
        try {
            InputStream in = Bot.class.getResourceAsStream(f);
            if (in == null) {
                System.err.println(String.format("resource '%s' not found, not using the .jar? version will be wrong", f));
                return;
            }
            BufferedReader b = new BufferedReader(new InputStreamReader(in));
            if (b.ready()) {
                v = b.readLine();
            }
            if (v.contains("dirty") || v.contains("-")) {
                System.err.println(String.format("WARNING: using non-release version '%s'", v));
            }
            version = v;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(String.format("Failed to parse : %s'", v));
            e.printStackTrace();
        }
    }

    public void setup() {
        try (InputStream input = new FileInputStream("photobot.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            token = prop.getProperty("token");
            String sids = prop.getProperty("ids");
            if (sids != null) {
                ids.addAll(Arrays.stream(sids.split(",")).mapToLong(Long::parseLong).boxed().collect(Collectors.toSet()));
            }
            log.debug("token:" + token);
            log.info("ids:" + ids);
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

    public static void main(String args[]) {
        System.out.println("Bot " + version());
        Configurator.setRootLevel(Level.DEBUG);

        Bot b = new Bot();
        b.setup();
    }
}

