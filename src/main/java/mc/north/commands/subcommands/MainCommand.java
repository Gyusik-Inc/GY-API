package mc.north.commands.subcommands;

import lombok.Getter;
import mc.north.utilites.chat.MessageUtil;
import mc.north.utilites.cmd.FilterUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class MainCommand implements TabExecutor {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    protected final MessageUtil messageUtil;
    @Getter
    private final String mainCommand;

    protected MainCommand(String mainCommand, MessageUtil messageUtil) {
        this.mainCommand = mainCommand.toLowerCase();
        this.messageUtil = messageUtil;
    }

    public void registerSubCommand(SubCommand sub) {
        CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
        if (info != null) {
            subCommands.put(info.name().toLowerCase(), sub);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!command.getName().equalsIgnoreCase(mainCommand)) return false;

        if (args.length < 1) {
            messageUtil.sendMessage(sender, "Доступные действия: " + messageUtil.getMainColor() + subCommands.keySet());
            return true;
        }

        String subName = args[0].toLowerCase();
        SubCommand sub = subCommands.get(subName);

        if (sub == null) {
            messageUtil.sendMessage(sender, "&cДействие не найдено.");
            return true;
        }

        CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
        if (info != null && !info.permission().isEmpty()) {
            if (sender instanceof Player player && !player.hasPermission(info.permission())) {
                messageUtil.sendPermissionMessage(sender);
                return true;
            }
        }

        sub.onCommand(sender, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase(mainCommand)) return Collections.emptyList();

        if (args.length == 1) {
            List<String> available = new ArrayList<>();
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                if (hasPermission(sender, entry.getValue())) {
                    available.add(entry.getKey());
                }
            }
            return FilterUtil.filterCompletions(available, args[0]);
        }

        if (args.length > 1) {
            SubCommand sub = subCommands.get(args[0].toLowerCase());
            if (sub != null && hasPermission(sender, sub)) {
                return sub.onTabComplete(sender, args);
            }
        }

        return Collections.emptyList();
    }

    private boolean hasPermission(CommandSender sender, SubCommand sub) {
        CommandInfo info = sub.getClass().getAnnotation(CommandInfo.class);
        return info == null || info.permission().isEmpty() || (sender instanceof Player player && player.hasPermission(info.permission()));
    }
}