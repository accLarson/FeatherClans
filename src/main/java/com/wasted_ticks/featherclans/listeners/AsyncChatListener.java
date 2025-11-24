package com.wasted_ticks.featherclans.listeners;

import com.wasted_ticks.featherclans.FeatherClans;
import com.wasted_ticks.featherclans.commands.AllyChatCommand;
import com.wasted_ticks.featherclans.commands.ChatCommand;
import com.wasted_ticks.featherclans.managers.ChatToggleManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AsyncChatListener implements Listener {
    
    private final FeatherClans plugin;
    private final ChatToggleManager chatToggleManager;
    private final ChatCommand chatCommand;
    private final AllyChatCommand allyChatCommand;
    
    public AsyncChatListener(FeatherClans plugin) {
        this.plugin = plugin;
        this.chatToggleManager = plugin.getChatToggleManager();
        this.chatCommand = new ChatCommand(plugin);
        this.allyChatCommand = new AllyChatCommand(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        
        // Check if player has clan chat toggled
        if (chatToggleManager.hasClanChatEnabled(player.getUniqueId())) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            
            // Build args array: ["chat", word1, word2, ...]
            List<String> argsList = new ArrayList<>();
            argsList.add("chat");
            argsList.addAll(Arrays.asList(message.split(" ")));
            String[] args = argsList.toArray(new String[0]);
            
            chatCommand.onCommand(player, null, "chat", args);
            return;
        }
        
        // Check if player has ally chat toggled
        if (chatToggleManager.hasAllyChatEnabled(player.getUniqueId())) {
            event.setCancelled(true);
            String message = PlainTextComponentSerializer.plainText().serialize(event.message());
            
            // Build args array: ["allychat", word1, word2, ...]
            List<String> argsList = new ArrayList<>();
            argsList.add("allychat");
            argsList.addAll(Arrays.asList(message.split(" ")));
            String[] args = argsList.toArray(new String[0]);
            
            allyChatCommand.onCommand(player, null, "allychat", args);
            return;
        }
    }
}
