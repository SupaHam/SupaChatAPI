package com.supaham.supachatapi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.supaham.supachatapi.fanciful.FancyMessage;
import org.bukkit.ChatColor;

/**
* Represents a class that transforms a {@link String} into a {@link FancyMessage}. Useful for fixing multicolor lines.
*/
public class SafeFancyMessage {
    private String message;
    private FancyMessage fancyMessage;
    private int currentIndex;

    private final Pattern INCREMENTAL_PATTERN = Pattern.compile(
            "(" + ChatColor.COLOR_CHAR +
            "[0-9a-fk-or])|(\\n)|(?:(https?://[^ ][^ ]*?)(?=[\\.\\?!,;:]?(?:[ \\n]|$)))",
            Pattern.CASE_INSENSITIVE
    );

    public SafeFancyMessage(String message, FancyMessage fancyMessage) {
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
