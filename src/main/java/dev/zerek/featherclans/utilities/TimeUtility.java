package dev.zerek.featherclans.utilities;

public class TimeUtility {

    /**
     * Calculates the number of days since a timestamp
     * @param timestamp The timestamp in milliseconds
     * @return The number of days since the timestamp
     */
    public static int getDaysSince(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;
        return (int) (timeDifference / (1000 * 60 * 60 * 24)); // Convert milliseconds to days
    }

    /**
     * Formats a timestamp into a human-readable "last seen" format
     * @param timestamp The timestamp in milliseconds
     * @return A formatted string representing how long ago the timestamp was
     */
    public static String formatTimeSince(long timestamp) {
        int days = getDaysSince(timestamp);
        if (days < 1) return "today";
        return days + " d";
    }
}
