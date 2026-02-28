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

    public MessageUtil(String prefix) {
        setPrefix(prefix);
    }

    public void setPrefix(String prefix) {
        this.prefix = colorize(prefix);
    }

    public String getPrefix() {
        return prefix;
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(prefix + message));
    }

    public void sendPermissionMessage(CommandSender sender) {
        sendMessage(sender, "&cВам нельзя этого делать!");
        playErrorSound(sender);
    }

    public void sendUnknownPlayerMessage(CommandSender sender, String unkPlayer) {
        sendMessage(sender, "Игрок '" + unkPlayer + "' не найден.");
        playErrorSound(sender);
    }

    public void sendUsageMessage(CommandSender sender, String command) {
        sendMessage(sender, "Использование: " + command);
        playErrorSound(sender);
    }

    public void sendCooldownMessage(Player player, long time) {
        sendMessage(player, "Подождите ещё " + MathUtil.formatTime(time));
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

}