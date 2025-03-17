package com.wasted_ticks.featherclans.utilities;

public class TimeUtility {

    /**
     * Formats a timestamp into a human-readable "last seen" format
     * @param timestamp The timestamp in milliseconds
     * @return A formatted string representing how long ago the timestamp was
     */
    public static String formatTimeSince(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - timestamp;
        
        // Check if within 24 hours
        long hours = timeDifference / (60 * 60 * 1000);
        if (hours < 24) return "today";

        // Convert to days and larger units
        long days = hours / 24;
        long months = days / 30;
        long years = days / 365;
        
        // Format based on the most appropriate time unit
        if (days < 30) return days + " dy" + (days == 1 ? "" : "s");
        else if (months < 12) return months + " mo" + (months == 1 ? "" : "s");
        else return years + " yr" + (years == 1 ? "" : "s");
    }
}
