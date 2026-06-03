package dev.zerek.featherclans.managers;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatToggleManager {
    
    private final Set<UUID> clanChatToggles = ConcurrentHashMap.newKeySet();
    private final Set<UUID> allyChatToggles = ConcurrentHashMap.newKeySet();
    private final Set<UUID> chatHidden = ConcurrentHashMap.newKeySet();
    
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
            chatHidden.remove(uuid); // Un-hide chat when entering send mode
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
            chatHidden.remove(uuid); // Un-hide chat when entering send mode
            return true;
        }
    }

    /**
     * Sets hidden chat mode for a player. When hidden, the player receives no
     * clan or ally chat messages.
     *
     * @param uuid player UUID
     * @param hidden true to hide clan and ally chat, false to show it
     */
    public void setChatHidden(UUID uuid, boolean hidden) {
        if (hidden) {
            chatHidden.add(uuid);
        } else {
            chatHidden.remove(uuid);
        }
    }

    /**
     * Clears all chat state (send toggles and hidden flag) for a player. Called
     * when a player leaves or is kicked from a clan so they are not stuck
     * redirecting chat to a clan they no longer belong to.
     *
     * @param uuid player UUID
     */
    public void clearToggles(UUID uuid) {
        clanChatToggles.remove(uuid);
        allyChatToggles.remove(uuid);
        chatHidden.remove(uuid);
    }

    /**
     * Disables ally chat mode for a player. Called when the player's clan loses
     * its ally so they are not stuck redirecting chat to a defunct alliance.
     *
     * @param uuid player UUID
     */
    public void disableAllyChat(UUID uuid) {
        allyChatToggles.remove(uuid);
    }

    public boolean hasClanChatEnabled(UUID uuid) {
        return clanChatToggles.contains(uuid);
    }

    public boolean hasAllyChatEnabled(UUID uuid) {
        return allyChatToggles.contains(uuid);
    }

    public boolean isChatHidden(UUID uuid) {
        return chatHidden.contains(uuid);
    }
}
