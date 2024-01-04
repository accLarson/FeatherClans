# FeatherClans

FeatherClans is a spigot plugin for player clan management.

Original code written by [Wasted_Ticks](https://github.com/paper-19)  
Now maintained and updated by [Zerek](https://github.com/accLarson)

### Features

Support for MySQL/MariaDB or built in SQLite.  
Includes optional [Vault](https://github.com/milkbowl/Vault) support, if *'use-economy'* is set to *true* in config.yml.  
Includes optional [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) support.  

### Placeholders:  
    %featherclans_clan%                 (players clan tag followed by a space or empty string)
    %featherclans_clan_parenthesis%     (player clan tag within parenthesis or empty string)
    %featherclans_clan_brackets%        (player clan tag within brackets or empty string)
    %featherclans_is_clanmember%        (boolean for true or false)
### Permission Nodes:  
#### Player Permissions
    feather.clans.accept           -     Accept a clan invitation
    feather.clans.appoint          -     Appoint a member as the new leader (must be in clan and leader must be "inactive")
    feather.clans.chat             -     Send a message to your clan members (must be in clan)
    feather.clans.confer           -     Confer the clan to another clan member (must be clan leader)
    feather.clans.create           -     Create a new clan
    feather.clans.decline          -     Decline a clan invitation
    feather.clans.disband          -     Disband a clan (must be a clan leader)
    feather.clans.friendlyfire     -     Toggle your personal friendly fire setting (must be in clan)
    feather.clans.home             -     Teleport to clan home (must be in clan)
    feather.clans.invite           -     Invite a player to your clan (must be clan leader)
    feather.clans.kick             -     Kick a member from your clan (must be clan leader)
    feather.clans.list             -     List all clans
    feather.clans.resign           -     Resign from your clan (must be in clan)
    feather.clans.roster           -     List clan members of a specific clan
    feather.clans.sethome          -     Set the clan's home (must be clan leader)
#### Admin Permissions
    feather.clans.banner           -     Recieve a clan banner
    feather.clans.manage           -     Simulate all clan commands as if you were the clan leader (BE CAREFUL)


