package mc.north.utilites.chat;

import mc.north.utilites.math.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class MessageUtil {

    private String prefix;
    private String mainColor;
    private static final long PRIORITY_TIMEOUT = 1500;
    private static final Pattern HEX_PATTERN = Pattern.compile("&?#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");
    private final Map<UUID, PriorityData> currentPriority = new HashMap<>();

    private static class PriorityData {
        int priority;
        long timestamp;

        PriorityData(int priority) {
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public MessageUtil(String prefix, String mainColor) {
        setPrefix(prefix);
        setMainColor(mainColor);
    }

    public void setPrefix(String prefix) {
        this.prefix = colorize(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void setMainColor(String mainColor) {
        this.mainColor = colorize(mainColor);
    }

    public String getMainColor() {
        return mainColor;
    }

    public String getGYString(String message) {
        return colorize(prefix + message);
    }

    public String getMainColorString(String message) {
        return colorize(mainColor + message);
    }


    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(prefix + message));
    }

    public void sendMainColorMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(mainColor + message));
    }

    public void sendPermissionMessage(CommandSender sender) {
        sendMessage(sender, "&cВам нельзя этого делать!");
        playErrorSound(sender);
    }

    public void sendUnknownPlayerMessage(CommandSender sender, String unkPlayer) {
        sendMessage(sender, "Игрок '" + mainColor + unkPlayer + "&f' не найден.");
        playErrorSound(sender);
    }

    public void sendUsageMessage(CommandSender sender, String command) {
        sendMessage(sender, "Использование: " + mainColor + command);
        playErrorSound(sender);
    }

    public void sendCooldownMessage(Player player, long time) {
        sendMessage(player, "Подождите ещё " + mainColor + MathUtil.formatTime(time));
    }

    public void sendItemBar(Player player, String message, String name) {
        sendItemBar(player, message, name, 1);
    }

    public void sendItemBar(Player player, String message, String name, int priority) {
        sendActionBar(player, name + " &8» &f" + message, false, priority);
    }

    public void sendTitle(Player player, String message, String name) {
        player.sendTitle(colorize(mainColor + name), colorize(message), 10, 10, 10);
    }

    public void sendEnchantBar(Player player, String message, String name) {
        sendActionBar(player, mainColor + name + " &8» &f" + message, false, 1);
    }

    public void sendActionBar(Player player, String message, boolean usePrefix) {
        sendActionBar(player, message, usePrefix, 1);
    }

    public void sendActionBar(Player player, String message, boolean usePrefix, int priority) {
        UUID playerId = player.getUniqueId();
        PriorityData data = currentPriority.get(playerId);

        if (data != null) {
            if (System.currentTimeMillis() - data.timestamp > PRIORITY_TIMEOUT) {
                currentPriority.remove(playerId);
                data = null;
            } else if (data.priority > priority) {
                return;
            }
        }

        currentPriority.put(playerId, new PriorityData(priority));

        if (usePrefix) {
            player.sendActionBar(colorize(prefix + message));
        } else {
            player.sendActionBar(colorize(message));
        }
    }

    public void clearActionBar(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            player.sendActionBar(" ");
            currentPriority.remove(playerId);
        }
    }

    public void clearActionBarIfPriority(UUID playerId, int priority) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            PriorityData data = currentPriority.get(playerId);
            if (data != null && data.priority == priority) {
                player.sendActionBar(" ");
                currentPriority.remove(playerId);
            }
        }
    }

    private void playErrorSound(CommandSender sender) {
        if (sender instanceof Player player) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }

    public String getColoredString(String message) {
        return colorize(prefix + message);
    }

    public static String colorize(String text) {
        if (text == null) return null;
        text = translateHexColors(text);
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);

            if (hex.length() == 3) {
                hex = String.format("%c%c%c%c%c%c",
                        hex.charAt(0), hex.charAt(0),
                        hex.charAt(1), hex.charAt(1),
                        hex.charAt(2), hex.charAt(2));
            }

            StringBuilder minecraftHex = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                minecraftHex.append('§').append(c);
            }

            matcher.appendReplacement(result, Matcher.quoteReplacement(minecraftHex.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String getPrefixColors(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String coloredText = colorize(text);
        StringBuilder prefixColors = new StringBuilder();

        for (int i = 0; i < coloredText.length(); i++) {
            char c = coloredText.charAt(i);

            if (c == '§' && i + 1 < coloredText.length()) {
                char nextChar = coloredText.charAt(i + 1);
                if ("0123456789abcdefklmnorxABCDEFKLMNORX".indexOf(nextChar) != -1) {
                    if (nextChar == 'x' || nextChar == 'X') {
                        if (i + 13 < coloredText.length()) {
                            prefixColors.append(coloredText, i, i + 14);
                            i += 13;
                            continue;
                        }
                    } else {
                        prefixColors.append(coloredText, i, i + 2);
                        i++;
                        continue;
                    }
                }
            }

            if (!Character.isWhitespace(c) && c != '§') {
                break;
            }

            if (c != '§') {
                prefixColors.append(c);
            }
        }

        return prefixColors.toString();
    }
}