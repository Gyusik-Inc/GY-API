package mc.north.listeners;

import mc.north.utilites.version.VersionCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 28.02.2026
 */

public class BukkitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        VersionCache.cache(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        VersionCache.remove(e.getPlayer());
    }
}