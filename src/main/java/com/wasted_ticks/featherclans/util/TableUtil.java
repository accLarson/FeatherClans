package com.wasted_ticks.featherclans.util;

import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableUtil {

    private List<Row> table = new LinkedList<>();
    private int columns;
    private Map<String, Integer> dictionary = new HashMap<String, Integer>();
    private final static int SPACER = 8;

    public TableUtil(String... headers) {
        this.columns = headers.length;
        Row row = new Row(true, headers);
        table.add(row);
        init();
    }

    private void init() {
        File file = new File(Bukkit.getPluginManager().getPlugin("FeatherClans").getDataFolder(), "characters.yml");
        if (!file.exists()) {Bukkit.getPluginManager().getPlugin("FeatherClans").saveResource("characters.yml",false);}
        YamlConfiguration characters = YamlConfiguration.loadConfiguration(file);
        characters.getKeys(false).forEach(key -> dictionary.put((String) characters.get(key + ".character"), characters.getInt(key + ".width")));
    }

    public List<String> generate() {

        int[] widths = new int[columns];
        for(int i = 0; i < columns; i++) {
            widths[i] = this.getColumnWidth(i);
        }

        List<String> lines = new ArrayList<>();
        for (Row row : table) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < columns; i++) {
                String string = row.getTexts().get(i);
                builder.append(this.spacer(string, widths[i] + SPACER));
                if (i < columns - 1) builder.append("| ");
            }
            lines.add(builder.toString());
            if(row.isHeader()) {
                lines.add(ChatUtil.translateHexColorCodes("&#393C56•----------------------------------------------------•"));
            }
        }
        return lines;
    }

    private String spacer(String string, int width) {
        // Calculate width of given string
        int stringWidth = 0;
        for (char c : string.toCharArray()){
            stringWidth += dictionary.get(String.valueOf(c));
        }

        double difference = width - stringWidth;
        int addonSpaces;
        int addonBoldSpaces = 0;


        // Calculate how many regular and bold spaces to append to the given string to meet the requested length
        if (difference % 4 == 1 && difference >= 4){
            addonSpaces = (int) (Math.floor(difference/4) - 1);
            addonBoldSpaces = 1;
        }
        else if (difference % 4 == 2 && difference >= 8){
            addonSpaces = (int) (Math.floor(difference/4) - 2);
            addonBoldSpaces = 2;
        }
        else if (difference % 4 == 3 && difference >= 12){
            addonSpaces = (int) (Math.floor(difference/4) - 3);
            addonBoldSpaces = 3;
        }
        else{
            addonSpaces = (int) (difference/4);
        }

        // Append spaces and bold spaces to the end of the given string
        StringBuilder stringBuilder = new StringBuilder(string);
        for (int i = 0; i < addonSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(ChatColor.BOLD);
        for (int i = 0; i < addonBoldSpaces; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(ChatColor.RESET);

        return stringBuilder.toString();
    }

    private int getStringWidth(String string) {
        return string.chars().map(c -> {
            return dictionary.get(String.valueOf((char) c));
        }).reduce(0, (s, e) -> s + e);
    }

    private int getColumnWidth(int column) {
        List<Integer> widths = table.stream().map(row -> {
            return row.getTexts().get(column).chars().map(c -> {
                return dictionary.get(String.valueOf((char) c));
            }).reduce(0, (s, e) -> s + e);
        }).collect(Collectors.toList());
        return Collections.max(widths);
    }

    public void addRow(String... values) {
        Row row = new Row(false, values);
        table.add(row);
    }

    private class Row {

        private List<String> texts = new ArrayList<>();
        boolean header;

        public Row(boolean header, String... values) {
            for (String text : values) {
                texts.add(text == null ? "" : text);
            }
            this.header = header;
        }

        public List<String> getTexts() {
            return texts;
        }

        public boolean isHeader() {
            return header;
        }
    }
}
