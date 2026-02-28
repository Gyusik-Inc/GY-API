package mc.north.utilites.version;

import com.viaversion.viaversion.api.Via;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VersionCache {

    private static final Map<UUID, Integer> versions = new HashMap<>();

    public static void cache(Player player) {
        int protocol = Via.getAPI().getPlayerVersion(player.getUniqueId());
        versions.put(player.getUniqueId(), protocol);
    }

    public static int get(Player player) {
        return versions.getOrDefault(player.getUniqueId(), 0);
    }

    public static boolean supportsTextDisplay(Player player) {
        return get(player) >= 762;
    }

    public static void remove(Player player) {
        versions.remove(player.getUniqueId());
    }

}