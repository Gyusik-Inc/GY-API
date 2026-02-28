package mc.north.utilites.math;

/**
 * @author Gyusik - Я ебанутый помогите!
 * @since 22.01.2026
 */

public class MathUtil {
    public static boolean chance(double percent) {
        return Math.random() * 100 >= percent;
    }

    public static String formatTime(double seconds) {
        if (seconds < 60) {
            return String.format("%.1f сек.", seconds);
        }

        int totalSeconds = (int) seconds;

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" дн. ");
        if (hours > 0) sb.append(hours).append(" ч. ");
        if (minutes > 0) sb.append(minutes).append(" мин. ");
        if (secs > 0) sb.append(secs).append(" сек.");

        return sb.toString().trim();
    }


    public static String formatTime(long milliseconds) {
        return formatTime(milliseconds / 1000.0);
    }
}