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
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    protected static final Logger log = LogManager.getLogger();
    protected static String version = "ERROR:NO_VERSION";
    protected String token;
    protected Long id;

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
            id = Long.parseLong(prop.getProperty("id"));
            log.info("token:" + token);
            log.info("id:" + id);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(this);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        sendMsg(id, "Bot is now online");
    }

    public void test() {
        log.info("hello");
    }

    public void sendMsg(Long id, String text) {
        log.debug(String.format("sendMsg(%s,%s)", id.toString(), text));
        SendMessage message = new SendMessage();
        message.setChatId(id.toString());
        message.setText(text);

        // HTML can be embedded in replies if required
        message.setParseMode(ParseMode.HTML);

        try {
            execute(message); // Call this method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("onUpdateReceived:" + update);
        if (update.hasChannelPost()) {
            Long id = update.getChannelPost().getChatId();
            String text = update.getChannelPost().getText();
            sendMsg(id, String.format("did you say:'<pre>%s</pre>'? " + id, text));
        }
        if (update.hasMessage()) {
            Long id = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            sendMsg(id, String.format("did you say:'<pre>%s</pre>'", text));
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

