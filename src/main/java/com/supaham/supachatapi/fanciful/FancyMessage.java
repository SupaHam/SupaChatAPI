package com.supaham.supachatapi.fanciful;

import com.supaham.supachatapi.util.ReflectionUtil;
import org.bukkit.Achievement;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonWriter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a Minecraft chat message that can be fancified with hover & click events, colors, styles and more. <p/>
 * <p/>
 * This class uses reflection for some methods such as achievements (Objects, not names) and sending the packet to a
 * player.
 */
public class FancyMessage {

    private final List<MessagePart> messageParts;
    private String jsonString;
    private boolean dirty;

    private Class<?> nmsChatSerializer = ReflectionUtil.getNMSClass("ChatSerializer");
    private Class<?> nmsTagCompound = ReflectionUtil.getNMSClass("NBTTagCompound");
    private Class<?> nmsPacketPlayOutChat = ReflectionUtil.getNMSClass("PacketPlayOutChat");
    private Class<?> nmsAchievement = ReflectionUtil.getNMSClass("Achievement");
    private Class<?> nmsStatistic = ReflectionUtil.getNMSClass("Statistic");
    private Class<?> nmsItemStack = ReflectionUtil.getNMSClass("ItemStack");

    private Class<?> obcStatistic = ReflectionUtil.getOBCClass("CraftStatistic");
    private Class<?> obcItemStack = ReflectionUtil.getOBCClass("inventory.CraftItemStack");

    public FancyMessage(final String firstPartText) {
        messageParts = new ArrayList<MessagePart>();
        messageParts.add(new MessagePart(firstPartText));
        jsonString = null;
        dirty = false;
    }

    public FancyMessage() {
        messageParts = new ArrayList<MessagePart>();
        messageParts.add(new MessagePart());
        jsonString = null;
        dirty = false;
    }

    /**
     * Sets the text of the current MessagePart.
     *
     * @param text text to set
     * @return this instance of FancyMessage, for chaining.
     * @throws IllegalStateException thrown if the text is already set for the message part.
     * @see #append(String)
     */
    public FancyMessage text(String text) throws IllegalStateException {
        MessagePart latest = latest();
        if (latest.hasText()) {
            throw new IllegalStateException("text for this message part is already set");
        }
        latest.text = text;
        dirty = true;
        return this;
    }

    /**
     * Appends text to this FancyMessage. If the text is already set for this MessagePart it will clone the
     * MessagePart to keep styles and colors.
     *
     * @param text text to append
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage append(String text) {
        MessagePart latest = latest();
        if (latest.hasText()) {
            latest = latest.clone();
            messageParts.add(latest);
        }
        latest.text = text;
        dirty = true;
        return this;
    }

    /**
     * Sets the {@link ChatColor} of this MessagePart.
     *
     * @param color color to set
     * @return this instance of FancyMessage, for chaining.
     * @throws IllegalArgumentException thrown if {@code color} is not a color
     */
    public FancyMessage color(final ChatColor color) throws IllegalArgumentException {
        if (!color.isColor()) {
            throw new IllegalArgumentException(color.name() + " is not a color");
        }
        latest().color = color;
        dirty = true;
        return this;
    }

    /**
     * Applies {@link ChatColor} styles to this MessagePart.
     *
     * @param styles array of styles to apply
     * @return this instance of FancyMessage, for chaining.
     * @throws IllegalArgumentException thrown if {@code styles} contains a color
     */
    public FancyMessage style(ChatColor... styles) throws IllegalArgumentException {
        for (final ChatColor style : styles) {
            if (!style.isFormat()) {
                throw new IllegalArgumentException(style.name() + " is not a style");
            }
        }
        latest().styles.addAll(Arrays.asList(styles));
        dirty = true;
        return this;
    }

    /**
     * Opens a file (client-side) on click event.
     *
     * @param path the path of the file to open
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage file(final String path) {
        onClick("open_file", path);
        return this;
    }

    /**
     * Opens a URL on click event.
     *
     * @param url url to open
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage link(final String url) {
        onClick("open_url", url);
        return this;
    }

    /**
     * Suggests a command on click event. This opens the player's chat box and inserts the given String.
     *
     * @param command command to suggest
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage suggest(final String command) {
        onClick("suggest_command", command);
        return this;
    }

    /**
     * Executes a command on click event. It should be noted that the commands executed are run by the player,
     * thus appearing in their recent chat history.
     *
     * @param command command to execute
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage command(final String command) {
        onClick("run_command", command);
        return this;
    }

    /**
     * Displays an achievement on hover event.
     *
     * @param name name of the achievement to display
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage achievementTooltip(final String name) {
        onHover("show_achievement", "achievement." + name);
        return this;
    }

    /**
     * Displays an {@link Achievement} on hover event.
     *
     * @param which achievement to display
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage achievementTooltip(final Achievement which) {
        try {
            Object achievement = ReflectionUtil.getMethod(obcStatistic, "getNMSAchievement").invoke(null, which);
            return achievementTooltip((String) ReflectionUtil.getField(nmsAchievement, "name").get(achievement));
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    /**
     * Displays a statistic on hover event.
     *
     * @param which statistic to display
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage statisticTooltip(final Statistic which) {
        Type type = which.getType();
        if (type != Type.UNTYPED) {
            throw new IllegalArgumentException("That statistic requires an additional " + type + " parameter!");
        }
        try {
            Object statistic = ReflectionUtil.getMethod(obcStatistic, "getNMSStatistic").invoke(null, which);
            return achievementTooltip((String) ReflectionUtil.getField(nmsStatistic, "name").get(statistic));
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    /**
     * Displays a statistic that requires a {@link Material} parameter on hover event.
     *
     * @param statistic statistic to display
     * @param material  material to pass to the {@code statistic}
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage statisticTooltip(final Statistic statistic, Material material) {
        Type type = statistic.getType();
        if (type == Type.UNTYPED) {
            throw new IllegalArgumentException("That statistic needs no additional parameter!");
        }
        if ((type == Type.BLOCK && material.isBlock()) || type == Type.ENTITY) {
            throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
        }
        try {
            Object obcStatistic = ReflectionUtil.getMethod(this.obcStatistic, "getMaterialStatistic")
                    .invoke(null, statistic, material);
            return achievementTooltip((String) ReflectionUtil.getField(nmsStatistic, "name").get(obcStatistic));
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    /**
     * Displays a statistic that requires a {@link EntityType} parameter on hover event.
     *
     * @param statistic statistic to display
     * @param entity    entity to pass to the {@code statistic}
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage statisticTooltip(final Statistic statistic, EntityType entity) {
        Type type = statistic.getType();
        if (type == Type.UNTYPED) {
            throw new IllegalArgumentException("That statistic needs no additional parameter!");
        }
        if (type != Type.ENTITY) {
            throw new IllegalArgumentException("Wrong parameter type for that statistic - needs " + type + "!");
        }
        try {
            Object obcStatistic = ReflectionUtil.getMethod(this.obcStatistic, "getEntityStatistic")
                    .invoke(null, statistic, entity);
            return achievementTooltip((String) ReflectionUtil.getField(nmsStatistic, "name").get(obcStatistic));
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    /**
     * Displays an Item written in json on hover event.
     *
     * @param itemJSON item's json to display
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage itemTooltip(final String itemJSON) {
        onHover("show_item", itemJSON);
        return this;
    }

    /**
     * Displays an {@link ItemStack} on hover event.
     *
     * @param itemStack
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage itemTooltip(final ItemStack itemStack) {
        try {
            Object nmsItem =
                    ReflectionUtil.getMethod(obcItemStack, "asNMSCopy", ItemStack.class).invoke(null, itemStack);
            return itemTooltip(
                    ReflectionUtil.getMethod(nmsItemStack, "save").invoke(nmsItem, nmsTagCompound.newInstance())
                            .toString()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    /**
     * Displays a tooltip on hover event.
     *
     * @param text text to display
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage tooltip(final String text) {
        return tooltip(text.split("\\n"));
    }

    /**
     * Displays a list of lines on hover event.
     *
     * @param lines lines to display
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage tooltip(final List<String> lines) {
        return tooltip((String[]) lines.toArray());
    }

    /**
     * Displays a list of lines on hover event.
     *
     * @param lines
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage tooltip(final String... lines) {
        if (lines.length == 1) {
            onHover("show_text", lines[0]);
        } else {
            itemTooltip(makeMultilineTooltip(lines));
        }
        return this;
    }

    /**
     * Creates a new MessagePart and appends an {@link Object} to it.
     *
     * @param obj object to append (toString() is called)
     * @return this instance of FancyMessage, for chaining.
     * @throws IllegalStateException thrown if the latest MessagePart doesn't have text
     */
    public FancyMessage then(final Object obj) throws IllegalStateException {
        if (!latest().hasText()) {
            throw new IllegalStateException("previous message part has no text");
        }
        messageParts.add(new MessagePart(obj.toString()));
        dirty = true;
        return this;
    }

    /**
     * Creates a new MessagePart.
     *
     * @return this instance of FancyMessage, for chaining.
     */
    public FancyMessage then() {
        if (!latest().hasText()) {
            throw new IllegalStateException("previous message part has no text");
        }
        messageParts.add(new MessagePart());
        dirty = true;
        return this;
    }

    /**
     * Creates a click event.
     *
     * @param name name of the event
     * @param data data to pass to the event
     */
    public void onClick(final String name, final String data) {
        final MessagePart latest = latest();
        latest.clickActionName = name;
        latest.clickActionData = data;
        dirty = true;
    }

    /**
     * Creates a hover event.
     *
     * @param name name of the event
     * @param data data to pass to the event
     */
    public void onHover(final String name, final String data) {
        final MessagePart latest = latest();
        latest.hoverActionName = name;
        latest.hoverActionData = data;
        dirty = true;
    }

    /**
     * Checks whether the current MessagePart has text.
     *
     * @return whether the current MessagePart has text
     */
    public boolean hasText() {
        return latest().hasText();
    }

    /**
     * Converts this FancyMessage to a JSON String.
     *
     * @return JSON of this fancy message
     */
    public String toJSONString() {
        if (!dirty && jsonString != null) {
            return jsonString;
        }
        StringWriter string = new StringWriter();
        JsonWriter json = new JsonWriter(string);
        try {
            if (messageParts.size() == 1) {
                latest().writeJson(json);
            } else {
                json.beginObject().name("text").value("").name("extra").beginArray();
                for (final MessagePart part : messageParts) {
                    part.writeJson(json);
                }
                json.endArray().endObject();
                json.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("invalid message");
        }
        jsonString = string.toString();
        dirty = false;
        System.out.println(jsonString);
        return jsonString;
    }

    /**
     * Sends this FancyMessage to a {@link Player}.
     *
     * @param player player to send message to
     */
    public void send(Player player) {
        try {
            Object handle = ReflectionUtil.getHandle(player);
            Object connection = ReflectionUtil.getField(handle.getClass(), "playerConnection").get(handle);
            Object serialized =
                    ReflectionUtil.getMethod(nmsChatSerializer, "a", String.class).invoke(null, toJSONString());
            Object packet = nmsPacketPlayOutChat.getConstructor(ReflectionUtil.getNMSClass("IChatBaseComponent"))
                    .newInstance(serialized);
            ReflectionUtil.getMethod(connection.getClass(), "sendPacket").invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends this FancyMessage to a {@link Player}s.
     *
     * @param players players to send this message to
     */
    public void send(final Iterable<Player> players) {
        for (final Player player : players) {
            send(player);
        }
    }

    private MessagePart latest() {
        return messageParts.get(messageParts.size() - 1);
    }

    private String makeMultilineTooltip(final String[] lines) {
        StringWriter string = new StringWriter();
        JsonWriter json = new JsonWriter(string);
        try {
            json.beginObject().name("id").value(1);
            json.name("tag").beginObject().name("display").beginObject();
            json.name("Name").value("\\u00A7f" + lines[0].replace("\"", "\\\""));
            json.name("Lore").beginArray();
            for (int i = 1; i < lines.length; i++) {
                final String line = lines[i];
                json.value(line.isEmpty() ? " " : line.replace("\"", "\\\""));
            }
            json.endArray().endObject().endObject().endObject();
            json.close();
        } catch (Exception e) {
            throw new RuntimeException("invalid tooltip");
        }
        return string.toString();
    }
}
