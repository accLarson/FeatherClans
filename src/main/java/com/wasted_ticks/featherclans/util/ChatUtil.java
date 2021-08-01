package com.wasted_ticks.featherclans.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtil {

    public static final Pattern HEXPattern = Pattern.compile("(#[a-fA-F0-9]{6})");

    public static String translateHexColorCodes(String message) {

        Matcher matcher = HEXPattern.matcher(message);
        while(matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            message = message.replace(color, ChatColor.of(color) + "");
            matcher = HEXPattern.matcher(message);
        }

        return ChatColor.translateAlternateColorCodes( '&', message);
    }
}
