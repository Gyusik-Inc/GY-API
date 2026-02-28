package mc.north.utilites.math;

import mc.north.GYAPI;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class TimeUtil {

    public static void smoothTimeTransition(List<World> worlds, long targetTime, long speed) {
        for (World world : worlds) {
            long currentTime = world.getTime();
            long distance = (targetTime - currentTime + 24000) % 24000;

            new BukkitRunnable() {
                long time = currentTime;
                long passed = 0;

                @Override
                public void run() {
                    if (passed >= distance) {
                        world.setTime(targetTime);
                        cancel();
                        return;
                    }
                    time = (time + speed) % 24000;
                    world.setTime(time);
                    passed += speed;
                }
            }.runTaskTimer(GYAPI.getInstance(), 0L, 1L);
        }
    }


    public static void smoothTimeTransition(World world, long targetTime, long speed) {
        smoothTimeTransition(List.of(world), targetTime, speed);
    }
}
