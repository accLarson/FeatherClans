# FeatherClans

FeatherClans is a spigot plugin for player clan management.  

### Features

Support for MySQL/MariaDB or built in SQLite.  
Includes optional [Vault](https://github.com/milkbowl/Vault) support, if *'use-economy'* is set to *true* in config.yml.  
Includes optional [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) support.  

### Placeholders:  
    %featherclans_clan%                (player's clan tag or empty string)
    %featherclans_clan_formatted%      (player's colored tag if exists and active otherwise unformatted clan tag)
    %featherclans_is_clanmember%       (PAPI boolean for true or false)
    %featherclans_clan_parenthesis%    (player's clan tag with parenthesis)
    %featherclans_clan_brackets%       (player's clan tag with brackets)
    %featherclans_clan_role%           (player's clan role indicator [leader|officer] or empty string)
### Permission Nodes:  
    feather.clans.accept              -     Accept a clan invitation
    feather.clans.ally                -     propose and disolve alliances (must be clan leader)
    feather.clans.banner              -     Recieve a clans banner (Admin)
    feather.clans.chat                -     Send a message to your clan members (must be in clan)
    feather.clans.confer              -     Confer the clan to another clan member (must be clan leader)
    feather.clans.create              -     Create a new clan
    feather.clans.decline             -     Decline a clan invitation
    feather.clans.disband             -     Disband a clan (must be a clan leader)
    feather.clans.friendlyfire        -     Toggle your personal friendly fire setting (must be in clan)
    feather.clans.forcefriendlyfire   -     Players with this permission allow friendly fire
    feather.clans.home                -     Teleport to clan home (must be in clan)
    feather.clans.invite              -     Invite a player to your clan (must be clan leader)
    feather.clans.kick                -     Kick a member from your clan (must be clan leader)
    feather.clans.list                -     List all clans
    feather.clans. officer            -     promote and demote officers (must be clan leader)
    feather.clans.resign              -     Resign from your clan (must be in clan)
    feather.clans.roster              -     List clan members of a specific clan
    feather.clans.sethome             -     Set the clan's home (must be clan leader)
    feather.clans.setarmor            -     Set the clan's armor for the Display (must be clan leader)
    feather.clans.setbanner           -     Set the clan's banner (must be clan leader)
    feather.clans.updatedisplay       -     Trigger the clan display to update (admin)