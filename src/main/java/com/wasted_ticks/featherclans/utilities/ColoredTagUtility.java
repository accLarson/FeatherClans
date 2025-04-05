package com.wasted_ticks.featherclans.utilities;

import java.util.LinkedHashMap;
import java.util.Map;

public class ColoredTagUtility {
    private static final Map<String,String> colorMap = new LinkedHashMap<>();
    
    static {
        colorMap.put("&f", "<#FFFFFF>");
        colorMap.put("&g", "<#CCCCCC>");
        colorMap.put("&h", "<#9F9F9F>");
        colorMap.put("&i", "<#757575>");
        colorMap.put("&j", "<#947F6A>");
        colorMap.put("&s", "<#EBAD9D>");
        colorMap.put("&t", "<#E8B789>");
        colorMap.put("&u", "<#E6E2A3>");
        colorMap.put("&v", "<#A8CC94>");
        colorMap.put("&w", "<#98CBD9>");
        colorMap.put("&x", "<#8AA6D4>");
        colorMap.put("&y", "<#BA9BD4>");
        colorMap.put("&z", "<#E2A5CE>");
    }

    public static boolean isValidColoredTag(String coloredTag, String tag) {
        String strippedTag = coloredTag;

        for (String colorCode : colorMap.keySet()) {
            strippedTag = strippedTag.replace(colorCode, "");
        }
        return strippedTag.equalsIgnoreCase(tag);
    }

    public static String convert(String tag) {
        String convertedTag = tag;

        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            convertedTag = convertedTag.replace(entry.getKey(), entry.getValue());
        }
        return convertedTag;
    }

    public static String getColorOptions() {
        StringBuilder options = new StringBuilder();
        
        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            String colorCode = entry.getKey();
            String hexColor = entry.getValue();
            options.append(hexColor).append(colorCode).append(" ");
        }
        
        return options.toString().trim();
    }
}
