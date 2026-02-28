package mc.north.commands.basecommands;

import org.bukkit.command.CommandSender;
import java.util.List;

public interface BaseCommand {
    boolean execute(CommandSender sender, String label, String[] args);
    List<String> tabComplete(CommandSender sender, String alias, String[] args);
}