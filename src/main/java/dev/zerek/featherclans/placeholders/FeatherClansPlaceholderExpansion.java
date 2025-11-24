package dev.zerek.featherclans.placeholders;

import dev.zerek.featherclans.FeatherClans;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FeatherClansPlaceholderExpansion extends PlaceholderExpansion {

    private final FeatherClans plugin;

    public FeatherClansPlaceholderExpansion(FeatherClans plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return "Zerek";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // This is required or else PlaceholderAPI will unregister the Expansion on reload.
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if(params.equalsIgnoreCase("is_clanmember")){
            return plugin.getClanManager().isOfflinePlayerInClan(player) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }

        if(params.equalsIgnoreCase("clan_role")) {
            String clan = plugin.getClanManager().getClanByOfflinePlayer(player);
            if (clan == null) return "";
            else if (plugin.getClanManager().isOfflinePlayerOfficer(player)) return plugin.getFeatherClansConfig().getOfficerIndicator();
            else if (plugin.getClanManager().isOfflinePlayerLeader(player)) return plugin.getFeatherClansConfig().getLeaderIndicator();
            else return " ";
        }

        if(params.equalsIgnoreCase("clan")) {
            String clan = plugin.getClanManager().getClanByOfflinePlayer(player);
            return (clan != null) ? clan : "";
        }

        if(params.equalsIgnoreCase("clan_formatted")) {
            String clan = plugin.getClanManager().getClanByOfflinePlayer(player);
            if (clan == null) return "";
            String coloredTag = plugin.getClanManager().getColorTag(clan);
            return (coloredTag != null) ? coloredTag : clan;
        }

        if(params.equalsIgnoreCase("clan_parenthesis")) {
            String clan = plugin.getClanManager().getClanByOfflinePlayer(player);
            return (clan != null) ? "(" + clan + ")" : "";
        }

        if(params.equalsIgnoreCase("clan_brackets")) {
            String clan = plugin.getClanManager().getClanByOfflinePlayer(player);
            return (clan != null) ? "[" + clan + "]" : "";
        }


        return null;
    }
}
