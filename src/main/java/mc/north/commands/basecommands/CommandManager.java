package mc.north.commands.basecommands;

import lombok.Getter;
import mc.north.utilites.chat.MessageUtil;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandManager {

    private final JavaPlugin plugin;
    private final MessageUtil messageUtil;

    @Getter
    private final Map<String, Long> cooldowns = new HashMap<>();

    public CommandManager(JavaPlugin plugin, MessageUtil messageUtil) {
        this.plugin = plugin;
        this.messageUtil = messageUtil;
    }

    public void registerCommand(Class<? extends BaseCommand> commandClass) {
        BaseCommandInfo annotation = commandClass.getAnnotation(BaseCommandInfo.class);
        if (annotation == null) return;

        String commandName = annotation.name();

        CommandExecutor executor = new CommandExecutorWrapper(plugin, commandClass, messageUtil);
        TabCompleter tabCompleter = (sender, command, alias, args) -> {
            try {
                if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
                    return List.of();
                }

                BaseCommand cmd = commandClass.getDeclaredConstructor().newInstance();
                return cmd.tabComplete(sender, alias, args);
            } catch (Exception e) {
                e.printStackTrace();
                return List.of();
            }
        };

        if (plugin.getCommand(commandName) != null) {
            Objects.requireNonNull(plugin.getCommand(commandName)).setExecutor(executor);
            Objects.requireNonNull(plugin.getCommand(commandName)).setTabCompleter(tabCompleter);
        }
    }

    private static class CommandExecutorWrapper implements CommandExecutor {

        private final JavaPlugin plugin;
        private final Class<? extends BaseCommand> commandClass;
        private final BaseCommandInfo annotation;
        private final MessageUtil messageUtil;
        private final Map<String, Long> cooldowns;

        public CommandExecutorWrapper(JavaPlugin plugin, Class<? extends BaseCommand> commandClass, MessageUtil messageUtil) {
            this.plugin = plugin;
            this.commandClass = commandClass;
            this.annotation = commandClass.getAnnotation(BaseCommandInfo.class);
            this.messageUtil = messageUtil;
            this.cooldowns = new HashMap<>();
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
            if (!annotation.permission().isEmpty() && !sender.hasPermission(annotation.permission())) {
                messageUtil.sendPermissionMessage(sender);
                return true;
            }

            if (annotation.cooldown() > 0) {
                String key = getCooldownKey(sender);
                Long lastUse = cooldowns.get(key);
                if (lastUse != null) {
                    long now = System.currentTimeMillis();
                    long cooldownMs = annotation.cooldown() * 1000L;
                    if (now - lastUse < cooldownMs && sender instanceof Player player) {
                        double remaining = (cooldownMs - (now - lastUse)) / 1000.0;
                        messageUtil.sendCooldownMessage(player, (long) (remaining * 1000));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        return true;
                    }
                }
                cooldowns.put(key, System.currentTimeMillis());
            }

            try {
                BaseCommand cmd = commandClass.getDeclaredConstructor().newInstance();
                return cmd.execute(sender, label, args);
            } catch (Exception e) {
                messageUtil.sendMessage(sender, "&cОшибка команды!");
                e.printStackTrace();
            }

            return false;
        }

        private String getCooldownKey(CommandSender sender) {
            return (sender instanceof Player ? ((Player) sender).getUniqueId().toString() : sender.getName())
                    + "_" + annotation.name();
        }
    }
}