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

    private static class SafeFancyMessage {
        private String message;
        private FancyMessage fancyMessage;
        private int currentIndex;

        private final Pattern INCREMENTAL_PATTERN = Pattern.compile(
                "(" + ChatColor.COLOR_CHAR +
                "[0-9a-fk-or])|(\\n)|(?:(https?://[^ ][^ ]*?)(?=[\\.\\?!,;:]?(?:[ \\n]|$)))",
                Pattern.CASE_INSENSITIVE
        );

        private SafeFancyMessage(String message, FancyMessage fancyMessage) {
            this.message = message;
            this.fancyMessage = fancyMessage;

            Matcher matcher = INCREMENTAL_PATTERN.matcher(message);
            String match;
            while (matcher.find()) {
                int group = 0;
                while ((match = matcher.group(++group)) == null) {
                }
                appendMessage(matcher.start(group));
                switch (group) {
                    case 1: // Color/format group
                        ChatColor color = ChatColor.getByChar(match.charAt(1));
                        if (color.equals(ChatColor.RESET)) {
                            fancyMessage.then();
                        } else if (color.isFormat()) {
                            switch (color) {
                                case BOLD:
                                    fancyMessage.style(ChatColor.BOLD);
                                    break;
                                case ITALIC:
                                    fancyMessage.style(ChatColor.ITALIC);
                                    break;
                                case STRIKETHROUGH:
                                    fancyMessage.style(ChatColor.STRIKETHROUGH);
                                    break;
                                case UNDERLINE:
                                    fancyMessage.style(ChatColor.UNDERLINE);
                                    break;
                                case MAGIC:
                                    fancyMessage.style(ChatColor.MAGIC);
                                    break;
                                default:
                                    throw new UnsupportedOperationException("Unexpected ChatColor format " + color);
                            }
                        } else {
                            fancyMessage.color(color);
                        }
                        break;
                    case 2: // New line
                        fancyMessage.then();
                        break;
                    case 3: // URL
                        appendMessage(matcher.end(group));
                        fancyMessage.link(match);
                }
                currentIndex = matcher.end(group);
            }

            if (currentIndex < message.length()) {
                appendMessage(message.length());
            }
        }

        private void appendMessage(int index) {
            if (index <= currentIndex) {
                return;
            }

            String message = this.message.substring(currentIndex, index);
            this.currentIndex = index;
            fancyMessage.append(message);
        }
    }
}
