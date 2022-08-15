package com.wasted_ticks.featherclans.util;

import com.wasted_ticks.featherclans.FeatherClans;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;


public class PaginateUtil {

    private final FeatherClans plugin;
    private final Map<String, Object> f = new HashMap<String, Object>();
    MiniMessage mm = MiniMessage.builder().tags(
            TagResolver.builder()
                    .resolver(StandardTags.color())
                    .resolver(StandardTags.decorations())
                    .resolver(StandardTags.reset())
                    .resolver(StandardTags.newline())
                    .build()).build();

    public PaginateUtil(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();
    }

    public void init(){
        ConfigurationSection formatYml = plugin.getConfig().getConfigurationSection("page-formats");
        formatYml.getKeys(false).forEach(key -> f.put(key,formatYml.get(key)));
    }

    public boolean displayPage(String[] args, Player player, List<Component> lines){
        if (lines.size() > 0){
            int page = 1;
            boolean argsHasPageNumber = false;
            //check if last arg is a number
            if (args.length > 0 && args[args.length-1].chars().allMatch(Character::isDigit)) argsHasPageNumber = true;
            if (argsHasPageNumber) page = Integer.parseInt(args[args.length-1]);

            List<Component> formattedLines = new ArrayList<>();
            int linesPerPage = (Integer) f.get("lines-per-page");
            int pageTotal = (int) Math.ceil((double) lines.size() / linesPerPage);
            int start = linesPerPage * page - linesPerPage;


            if (lines.size() > start && pageTotal < 1000 && page != 0){
                formattedLines.add(mm.deserialize(f.get("table-prefix-line").toString()));
                int end = start + linesPerPage;
                if (lines.size() > end) formattedLines.addAll(lines.subList(start, end));
                else formattedLines.addAll(lines.subList(start, lines.size()));


                if (pageTotal > 1){
                    TextComponent backArrow = (TextComponent) mm.deserialize(f.get("back-arrow-disabled").toString());
                    TextComponent nextArrow = (TextComponent) mm.deserialize(f.get("next-arrow-disabled").toString());
                    String newCommandNoPage = "/clan " + String.join(" ",args);

                    if (argsHasPageNumber) newCommandNoPage = "/clan " + String.join(" ", Arrays.copyOf(args, args.length - 1));
                    if (page - 1 > 0) {
                        backArrow = (TextComponent) mm.deserialize(f.get("back-arrow").toString());
                        backArrow = backArrow.clickEvent(ClickEvent.runCommand(newCommandNoPage + " " + (page - 1)));
                    }
                    if (page < pageTotal) {
                        nextArrow = (TextComponent) mm.deserialize(f.get("next-arrow").toString());
                        nextArrow = nextArrow.clickEvent(ClickEvent.runCommand(newCommandNoPage + " " + (page + 1)));
                    }

                    TextComponent footer = (TextComponent) mm.deserialize(f.get("paginator-prefix").toString());
                    TextComponent pageCounter = (TextComponent) mm.deserialize(f.get("page-count-prefix") + String.format("%03d", page) + "/" + String.format("%03d", pageTotal) + f.get("page-count-suffix"));
                    footer = footer.append(backArrow).append(pageCounter).append(nextArrow).append(mm.deserialize(f.get("paginator-suffix").toString()));
                    formattedLines.add(footer);
                }
                else formattedLines.add(mm.deserialize(f.get("table-suffix-line").toString()));

                formattedLines.forEach(player::sendMessage);
                return true;
            }
        }
        return false;
    }
}
