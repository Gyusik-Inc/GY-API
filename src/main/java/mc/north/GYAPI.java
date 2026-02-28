package mc.north;

import lombok.Getter;
import mc.north.utilites.chat.MessageUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class GYAPI extends JavaPlugin {
    @Getter
    private static GYAPI instance;

    @Override
    public void onEnable() {
        instance = this;

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
