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
        
        // Convert to appropriate units
        long minutes = timeDifference / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;
        
        // Format based on the most appropriate time unit
        if (minutes < 60) {
            return minutes + " min" + (minutes == 1 ? "" : "s");
        } else if (hours < 24) {
            return hours + " hr" + (hours == 1 ? "" : "s");
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s");
        } else if (days < 30) {
            return weeks + " wk" + (weeks == 1 ? "" : "s");
        } else if (days < 365) {
            return months + " mon" + (months == 1 ? "" : "s");
        } else {
            return years + " yr" + (years == 1 ? "" : "s");
        }
    }
}
