package com.wasted_ticks.featherclans.utilities;

import com.wasted_ticks.featherclans.FeatherClans;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;

public class ColorTagUtil {

    private final FeatherClans plugin;
    private final Map<String, String> colorMap;
    private final MiniMessage mm = MiniMessage.miniMessage();

     {
        colorMap = new HashMap<>();
         colorMap.put("&g", "<#CCCCCC>");
         colorMap.put("&h", "<#757575>");
         colorMap.put("&i", "<#9F9F9F>");
         colorMap.put("&j", "<#A18870>");
         colorMap.put("&s", "<#DA9696>");
         colorMap.put("&t", "<#E6BE99>");
         colorMap.put("&u", "<#E6E2A3>");
         colorMap.put("&v", "<#A8CC94>");
         colorMap.put("&w", "<#A0CAD5>");
         colorMap.put("&x", "<#97AFD5>");
         colorMap.put("&y", "<#C29ACA>");
         colorMap.put("&z", "<#E2A5C3>");

    }

    public ColorTagUtil(FeatherClans plugin) {
        this.plugin = plugin;
    }

    public boolean isValid(String potentialTag, String originalClanTag) {
        String strippedClanTag = mm.stripTags(potentialTag);
        String clanTagWithHex = ConvertColorCodesToHexTags(strippedClanTag);

        return mm.stripTags(clanTagWithHex).equalsIgnoreCase(originalClanTag);
     }

    private  String removeSpaces(String text) {
        return text;
    }

    public String ConvertColorCodesToHexTags(String tag) {
        if (tag == null || tag.isEmpty()) return tag;

        for (Map.Entry<String, String> entry : colorMap.entrySet()) {
            tag = tag.replace(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    private String stripColorTags(String text) {
        // Remove the hex color tags to compare with the original clan tag
        return text.replaceAll("<#[a-fA-F0-9]{6}>", "");
    }
}
