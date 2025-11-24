package dev.zerek.featherclans.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatToggleManager {
    
    private final Set<UUID> clanChatToggles = new HashSet<>();
    private final Set<UUID> allyChatToggles = new HashSet<>();
    
    /**
     * Toggles clan chat mode for a player.
     * 
     * @param uuid player UUID
     * @return true if clan chat is now enabled, false if disabled
     */
    public boolean toggleClanChat(UUID uuid) {
        if (clanChatToggles.contains(uuid)) {
            clanChatToggles.remove(uuid);
            return false;
        } else {
            clanChatToggles.add(uuid);
            allyChatToggles.remove(uuid); // Disable ally chat if enabling clan chat
            return true;
        }
    }
    
    /**
     * Toggles ally chat mode for a player.
     * 
     * @param uuid player UUID
     * @return true if ally chat is now enabled, false if disabled
     */
    public boolean toggleAllyChat(UUID uuid) {
        if (allyChatToggles.contains(uuid)) {
            allyChatToggles.remove(uuid);
            return false;
        } else {
            allyChatToggles.add(uuid);
            clanChatToggles.remove(uuid); // Disable clan chat if enabling ally chat
            return true;
        }
    }
    
    public boolean hasClanChatEnabled(UUID uuid) {
        return clanChatToggles.contains(uuid);
    }
    
    public boolean hasAllyChatEnabled(UUID uuid) {
        return allyChatToggles.contains(uuid);
    }
}
