package dev.zerek.featherclans.utilities;

import dev.zerek.featherclans.FeatherClans;
import dev.zerek.featherclans.config.FeatherClansConfig;
import dev.zerek.featherclans.config.FeatherClansMessages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.entity.Player;

import java.util.*;


public class PaginateUtility {

    private final FeatherClans plugin;
    private FeatherClansMessages messages;
    private FeatherClansConfig config;


    public PaginateUtility(FeatherClans plugin) {
        this.plugin = plugin;
        this.init();
    }

    public void init(){
        this.messages = this.plugin.getFeatherClansMessages();
        this.config = this.plugin.getFeatherClansConfig();
    }

    public void displayPage(String[] args, Player player, List<Component> lines){
        if (!lines.isEmpty()){
            int page = 1;
            boolean argsHasPageNumber = args.length > 0 && args[args.length - 1].chars().allMatch(Character::isDigit);
            if (argsHasPageNumber) page = Integer.parseInt(args[args.length-1]);

            List<Component> formattedLines = new ArrayList<>();
            int linesPerPage = config.getLinesPerPage();
            int pageTotal = (int) Math.ceil((double) lines.size() / linesPerPage);
            int start = linesPerPage * page - linesPerPage;


            if (lines.size() > start && pageTotal < 1000 && page != 0){
                formattedLines.add(messages.get("clan_pre_line",null));
                int end = start + linesPerPage;
                if (lines.size() > end) formattedLines.addAll(lines.subList(start, end));
                else formattedLines.addAll(lines.subList(start, lines.size()));


                if (pageTotal > 1){

                    Component backArrow = messages.get("clan_back_arrow_disabled",null);
                    Component nextArrow = messages.get("clan_next_arrow_disabled",null);
                    String newCommandNoPage = "/clan " + String.join(" ",args);

                    if (argsHasPageNumber) newCommandNoPage = "/clan " + String.join(" ", Arrays.copyOf(args, args.length - 1));

                    if (page - 1 > 0) {
                        backArrow = messages.get("clan_back_arrow",null);
                        backArrow = backArrow.clickEvent(ClickEvent.runCommand(newCommandNoPage + " " + (page - 1)));
                    }

                    if (page < pageTotal) {
                        nextArrow = messages.get("clan_next_arrow",null);
                        nextArrow = nextArrow.clickEvent(ClickEvent.runCommand(newCommandNoPage + " " + (page + 1)));
                    }

                    Component footer = messages.get("clan_paginator_prefix",null);
                    Component pageCounter = messages.get("clan_page_count", Map.of(
                            "page", String.format("%03d", page),
                            "total", String.format("%03d", pageTotal)
                    ));

                    footer = footer.append(backArrow).append(pageCounter).append(nextArrow).append(messages.get("clan_paginator_suffix",null));
                    formattedLines.add(footer);
                }
                else formattedLines.add(messages.get("clan_line",null));

                formattedLines.forEach(player::sendMessage);
            }
        }
    }
}
