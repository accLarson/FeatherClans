# FeatherClans

FeatherClans is a Paper plugin for player clan management. Clans have leaders and officers, can form alliances, share a clan home, and use private clan and ally chat channels.

### Features

Support for MySQL/MariaDB or built in SQLite.
Includes optional [Vault](https://github.com/milkbowl/Vault) support, if *'use-economy'* is set to *true* in config.yml.
Includes optional [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) support.
Includes optional [LuckPerms](https://luckperms.net/) support.

### Placeholders:
    %featherclans_clan%                (player's clan tag or empty string)
    %featherclans_clan_formatted%      (player's colored tag if exists and active otherwise unformatted clan tag)
    %featherclans_is_clanmember%       (PAPI boolean for true or false)
    %featherclans_clan_parenthesis%    (player's clan tag with parenthesis)
    %featherclans_clan_brackets%       (player's clan tag with brackets)
    %featherclans_clan_role%           (player's clan role indicator [leader|officer] or empty string)

### Permission Nodes:

**Parent node — grants every node in the Player commands and Leadership commands sections below.** Leader-only and officer-only actions are still gated by the player's actual clan role in code, so it is safe to grant to all members.

    feather.clans.player              -     Grants access to the clan system

Player commands (all granted by `feather.clans.player`):

    feather.clans.home                -     Teleport to your clan home
    feather.clans.allyhome            -     Teleport to your ally's shared home
    feather.clans.accept              -     Accept a clan invitation
    feather.clans.decline             -     Decline a clan invitation
    feather.clans.resign              -     Resign from your clan
    feather.clans.roster              -     View clan rosters
    feather.clans.list                -     View the list of all clans
    feather.clans.lookup              -     Look up which clan a player is in
    feather.clans.friendlyfire        -     Toggle your personal friendly fire setting
    feather.clans.chat                -     Use clan chat and toggle automatic clan chat mode
    feather.clans.allychat            -     Use ally chat and toggle automatic ally chat mode

Leadership commands (all granted by `feather.clans.player`; role-enforced in code):

    feather.clans.create              -     Create a new clan
    feather.clans.invite              -     Invite players (leader/officer)
    feather.clans.kick                -     Kick members (leader/officer)
    feather.clans.sethome             -     Set your clan home (leader/officer)
    feather.clans.rally               -     Rally online clan members (leader/officer)
    feather.clans.confer              -     Transfer clan leadership (leader)
    feather.clans.officer             -     Promote or demote officers (leader)
    feather.clans.disband             -     Disband your clan (leader)
    feather.clans.setallyhome         -     Set the shared ally home (leader)
    feather.clans.setarmor            -     Set your clan armor (leader)
    feather.clans.setbanner           -     Set your clan banner (leader)
    feather.clans.settag              -     Set your clan's colored tag (leader)
    feather.clans.ally                -     Propose or dissolve alliances (leader)
    feather.clans.takeover            -     Take over an inactive leader's clan (officer)

Admin (default: op):

    feather.clans.banner              -     Retrieve any clan's banner
    feather.clans.manage              -     Manage any clan as an administrator
    feather.clans.reload              -     Reload plugin configuration
    feather.clans.debug               -     Use debug commands
    feather.clans.home.others         -     Teleport to another clan's home
    feather.clans.allyhome.others     -     Teleport to another clan's ally home

Modifier:

    feather.clans.forcefriendlyfire   -     Force friendly fire on, ignoring clan toggles
