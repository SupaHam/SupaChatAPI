package com.supaham.supachatapi.fanciful;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonWriter;

import java.util.ArrayList;

final class MessagePart implements Cloneable {

    ChatColor color = ChatColor.WHITE;
    ArrayList<ChatColor> styles = new ArrayList<ChatColor>();
    String clickActionName = null, clickActionData = null,
            hoverActionName = null, hoverActionData = null;
    String text = "";

    MessagePart(final String text) {
        this.text = text;
    }

    MessagePart() {
    }

    boolean hasText() {
        return !text.isEmpty();
    }

    @Override
    protected MessagePart clone() {
        try {
            super.clone();
            MessagePart part = new MessagePart();
            part.color = this.color;
            part.styles = this.styles;
            part.clickActionName = this.clickActionName;
            part.clickActionData = this.clickActionData;
            part.hoverActionName = this.hoverActionName;
            part.hoverActionData = this.hoverActionData;
            part.text = this.text;
            return part;
        } catch (CloneNotSupportedException ignored) {
            return null;
        }
    }

    JsonWriter writeJson(JsonWriter json) {
        try {
            json.beginObject().name("text").value(text);
            json.name("color").value(color.name().toLowerCase());
            for (final ChatColor style : styles) {
                String styleName;
                switch (style) {
                    case MAGIC:
                        styleName = "obfuscated";
                        break;
                    case UNDERLINE:
                        styleName = "underlined";
                        break;
                    default:
                        styleName = style.name().toLowerCase();
                        break;
                }
                json.name(styleName).value(true);
            }
            if (clickActionName != null && clickActionData != null) {
                json.name("clickEvent")
                        .beginObject()
                        .name("action").value(clickActionName)
                        .name("value").value(clickActionData)
                        .endObject();
            }
            if (hoverActionName != null && hoverActionData != null) {
                json.name("hoverEvent")
                        .beginObject()
                        .name("action").value(hoverActionName)
                        .name("value").value(hoverActionData)
                        .endObject();
            }
            return json.endObject();
        } catch (Exception e) {
            e.printStackTrace();
            return json;
        }
    }

}
