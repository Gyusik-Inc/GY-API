package mc.north.utilites.chat;

import mc.north.GYAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class AnimateGradientUtil {

    public static void animateGradientTitle(Player player,
                                            String edgeColor,
                                            String centerColor,
                                            String title,
                                            String subtitle,
                                            long durationMs) {
        new GradientTitleAnimator(player, edgeColor, centerColor, title, subtitle, durationMs, true).start();
    }

    public static void animateGradientTitleNoDelay(Player player,
                                                   String edgeColor,
                                                   String centerColor,
                                                   String title,
                                                   String subtitle,
                                                   long durationMs) {
        new GradientTitleAnimator(player, edgeColor, centerColor, title, subtitle, durationMs, true).startNoDelay();
    }

    public static void animateGradientFailTitle(Player player,
                                                String edgeColor,
                                                String centerColor,
                                                String title,
                                                String subtitle,
                                                long durationMs) {
        new GradientTitleAnimator(player, edgeColor, centerColor, title, subtitle, durationMs, false).start();
    }

    public static void animateGradientFailTitleNoDelay(Player player,
                                                       String edgeColor,
                                                       String centerColor,
                                                       String title,
                                                       String subtitle,
                                                       long durationMs) {
        new GradientTitleAnimator(player, edgeColor, centerColor, title, subtitle, durationMs, false).startNoDelay();
    }

    private static class GradientTitleAnimator implements Runnable {

        private final Player player;
        private final String edgeColor;
        private final String centerColor;
        private final String title;
        private final String subtitle;
        private final boolean successSound;

        private final int totalTicks;
        private final int subtitleTotalTicks;
        private final String staticTitle;

        private int tick = 0;
        private boolean subtitlePhase = false;
        private boolean finishing = false;

        private static final String SUB_BASE_COLOR = "#FFFFFF";
        private static final int START_DELAY_TICKS = 15;

        public GradientTitleAnimator(Player player,
                                     String edgeColor,
                                     String centerColor,
                                     String title,
                                     String subtitle,
                                     long durationMs,
                                     boolean successSound) {
            this.player = player;
            this.edgeColor = edgeColor;
            this.centerColor = centerColor;
            this.title = title;
            this.subtitle = subtitle;
            this.successSound = successSound;
            this.totalTicks = (int) (durationMs / 50L);
            this.subtitleTotalTicks = totalTicks;
            this.staticTitle = MessageUtil.colorize(edgeColor + "&l" + title);
        }

        @Override
        public void run() {
            if (!player.isOnline()) return;

            if (!subtitlePhase) {
                if (tick >= totalTicks) {
                    subtitlePhase = true;
                    tick = 0;
                    Bukkit.getScheduler().runTaskLater(GYAPI.getInstance(), this, 1);
                    return;
                }

                double progress = (double) tick / totalTicks;
                player.sendTitle(buildTitleFrame(progress), subtitle, 0, 25, 0);

                playSoundTick(tick, totalTicks / 2, 0);

                tick++;
                Bukkit.getScheduler().runTaskLater(GYAPI.getInstance(), this, 0);
                return;
            }

            if (!finishing) {
                if (tick >= subtitleTotalTicks) {
                    finishing = true;
                    String finalSubtitle = MessageUtil.colorize(SUB_BASE_COLOR + subtitle);
                    player.sendTitle(staticTitle, finalSubtitle, 0, 10, 40);

                    playSoundFinish();
                    return;
                }

                double progress = (double) tick / subtitleTotalTicks;
                player.sendTitle(staticTitle, buildSubtitleFrame(progress), 0, 20, 0);

                playSoundTick(tick, subtitleTotalTicks / 2, 1);

                tick++;
                Bukkit.getScheduler().runTaskLater(GYAPI.getInstance(), this, 1);
            }
        }

        public void start() {
            player.resetTitle();
            Bukkit.getScheduler().runTaskLater(
                    GYAPI.getInstance(),
                    () -> Bukkit.getScheduler().runTask(GYAPI.getInstance(), this),
                    START_DELAY_TICKS
            );
        }

        public void startNoDelay() {
            player.resetTitle();
            Bukkit.getScheduler().runTask(GYAPI.getInstance(), this);
        }

        private String buildTitleFrame(double progress) {
            int length = title.length();
            double center = (length - 1) / 2.0;
            double waveCenter = progress * 1.4;
            double softness = 0.18;

            StringBuilder out = new StringBuilder();
            for (int i = 0; i < length; i++) {
                double dist = Math.abs(i - center) / center;
                double d = dist - waveCenter;
                double t = Math.exp(-(d * d) / (2 * softness * softness));
                out.append(lerpColor(edgeColor, centerColor, t))
                        .append("§l")
                        .append(title.charAt(i));
            }
            return MessageUtil.colorize(out.toString());
        }

        private String buildSubtitleFrame(double progress) {
            int length = subtitle.length();
            double center = (length - 1) / 2.0;
            double wave = Math.sin(progress * Math.PI);
            double softness = 0.22;

            StringBuilder out = new StringBuilder();
            for (int i = 0; i < length; i++) {
                double dist = Math.abs(i - center) / center;
                double d = dist - (1.0 - wave);
                double t = Math.exp(-(d * d) / (2 * softness * softness));
                out.append(lerpColor(SUB_BASE_COLOR, centerColor, t))
                        .append(subtitle.charAt(i));
            }
            return MessageUtil.colorize(out.toString());
        }

        private String lerpColor(String c1, String c2, double t) {
            int r1 = Integer.parseInt(c1.substring(1, 3), 16);
            int g1 = Integer.parseInt(c1.substring(3, 5), 16);
            int b1 = Integer.parseInt(c1.substring(5, 7), 16);

            int r2 = Integer.parseInt(c2.substring(1, 3), 16);
            int g2 = Integer.parseInt(c2.substring(3, 5), 16);
            int b2 = Integer.parseInt(c2.substring(5, 7), 16);

            int r = (int) (r1 + (r2 - r1) * t);
            int g = (int) (g1 + (g2 - g1) * t);
            int b = (int) (b1 + (b2 - b1) * t);

            return String.format("#%02X%02X%02X", r, g, b);
        }

        private void playSoundTick(int tick, int midTick, int phase) {
            if (!successSound) {
                if (tick == 0 || tick == midTick) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                }
            } else {
                if (tick == 0 || tick == midTick) {
                    player.playSound(player.getLocation(), "minecraft:entity.experience_orb.pickup", 1.0f, 1.0f);
                }
            }
        }

        private void playSoundFinish() {
            if (!successSound) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }
}
