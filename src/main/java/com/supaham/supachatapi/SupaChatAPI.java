package com.supaham.supachatapi;

import com.supaham.supachatapi.fanciful.FancyMessage;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main class for the SupaChatAPI plugin. The plugin is not required to be enabled the API to function.
 */
public class SupaChatAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        // Nothing to do here.
    }

    /**
     * Appends a String to a {@link FancyMessage}.
     * <p/>
     * This method applies colors as JSON properties instead of part of the message. If the text property in JSON
     * contains a colour code, then it doesn't keep the color for more than one line, the line length is client side
     * and it is highly recommended this method be used instead of colors.
     * <p/>
     * This method is extremely useful when a message contains a URL. Without this method, the client would think the
     * URL is just text.
     *
     * @param message message to fancify
     * @return instance of FancyMessage containing fancified {@code message}
     */
    public static FancyMessage stringToFancyMessage(String message) {
        Validate.notNull(message, "message cannot be null.");
        FancyMessage fancyMessage = new FancyMessage();
        stringToFancyMessage(message, fancyMessage);
        return fancyMessage;
    }

    /**
     * Appends a String to a {@link FancyMessage}.
     * <p/>
     * This method applies colors as JSON properties instead of part of the message. If the text property in JSON
     * contains a colour code, then it doesn't keep the color for more than one line, the line length is client side
     * and it is highly recommended this method be used instead of colors.
     * <p/>
     * This method is extremely useful when a message contains a URL. Without this method, the client would think the
     * URL is just text.
     *
     * @param message      message to fancify
     * @param fancyMessage FancyMessage to apply fancified {@code message} to
     */
    public static void stringToFancyMessage(String message, FancyMessage fancyMessage) {
        Validate.notNull(message, "message cannot be null.");
        Validate.notNull(fancyMessage, "fancyMessage cannot be null.");
        new SafeFancyMessage(message, fancyMessage);
    }

}
