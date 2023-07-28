package net.jandie1505.cloudacm2.commands;

import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
import net.jandie1505.cloudacm2.lobby.LobbyPlayerData;
import net.jandie1505.cloudacm2.lobby.MapData;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ACM2Command implements CommandExecutor, TabCompleter {
    private final CloudACM2 plugin;

    public ACM2Command(CloudACM2 plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (args.length < 1) {
            sender.sendMessage("§Unknown option");
            return true;
        }

        switch (args[0]) {
            case "status" -> this.statusSubcommand(sender);
            case "stop" -> this.stopSubcommand(sender);
            case "start" -> this.startSubcommand(sender);
            case "bypass" -> this.bypassSubcommand(sender, args);
            case "map", "maps" -> this.mapsSubcommand(sender);
            case "forcemap" -> this.forcemapSubcommand(sender, args);
            case "votemap" -> this.votemapCommand(sender, args);
            case "player", "players" -> this.playerSubcommand(sender, args);
            case "cloudsystemmode" -> this.cloudsystemModeSubcommand(sender, args);
            default -> sender.sendMessage("§cUnknown subcommand");
        }

        return true;
    }

    public void statusSubcommand(CommandSender sender) {

        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage("§cNo Permission");
            return;
        }

        if (this.plugin.getGame() instanceof Lobby) {
            sender.sendMessage("§7Current status: LOBBY");
        } else if (this.plugin.getGame() instanceof Game) {
            sender.sendMessage("§7Current status: INGAME");
        } else if (this.plugin.getGame() == null) {
            sender.sendMessage("§7Current status: ---");
        } else {
            sender.sendMessage("§7UNKNOWN");
        }

    }

    public void stopSubcommand(CommandSender sender) {

        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage("§cNo permission");
            return;
        }

        this.plugin.stopGame();
        sender.sendMessage("§aStopped game");

    }

    public void startSubcommand(CommandSender sender) {

        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage("§cNo permission");
            return;
        }

        if (this.plugin.getGame() == null) {
            this.plugin.startGame();
            sender.sendMessage("§aStarted lobby");
        } else if (this.plugin.getGame() instanceof Lobby) {
            this.plugin.nextStatus();
            sender.sendMessage("§aStarted game");
        } else {
            sender.sendMessage("§cGame already running");
        }

    }

    public void bypassSubcommand(CommandSender sender, String[] args) {

        if (args.length == 1 && sender instanceof Player) {

            sender.sendMessage("§7Your bypass status: " + this.plugin.isPlayerBypassing(((Player) sender).getUniqueId()) + " " + this.plugin.getBypassingPlayers().contains(((Player) sender).getUniqueId()));

        } else {

            if (!this.hasAdminPermission(sender)) {
                sender.sendMessage("§cNo permission");
                return;
            }

            if (args.length == 2) {

                if (sender instanceof Player && (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true"))) {

                    this.plugin.addBypassingPlayer(((Player) sender).getUniqueId());
                    sender.sendMessage("§aBypassing mode enabled");

                } else if (sender instanceof Player && (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false"))) {

                    this.plugin.removeBypassingPlayer(((Player) sender).getUniqueId());
                    sender.sendMessage("§aBypassing mode disabled");

                } else {

                    UUID playerId;

                    try {
                        playerId = UUID.fromString(args[1]);
                    } catch (IllegalArgumentException e) {
                        OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(args[1]);

                        if (player == null) {
                            sender.sendMessage("§cUnknown player");
                            return;
                        }

                        playerId = player.getUniqueId();
                    }

                    sender.sendMessage("§7Bypassing status of the player: " + this.plugin.isPlayerBypassing(playerId) + " " + this.plugin.getBypassingPlayers().contains(playerId));

                }

            } else if (args.length == 3) {

                UUID playerId;

                try {
                    playerId = UUID.fromString(args[1]);
                } catch (IllegalArgumentException e) {
                    OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(args[1]);

                    if (player == null) {
                        sender.sendMessage("§cUnknown player");
                        return;
                    }

                    playerId = player.getUniqueId();
                }

                if (args[2].equalsIgnoreCase("on") || args[2].equalsIgnoreCase("true")) {

                    this.plugin.addBypassingPlayer(playerId);
                    sender.sendMessage("§aBypass for player enabled");

                } else if (args[2].equalsIgnoreCase("off") || args[2].equalsIgnoreCase("false")) {

                    this.plugin.removeBypassingPlayer(playerId);
                    sender.sendMessage("§aBypass for player disabled");

                } else {
                    sender.sendMessage("§cUsage: /combattest bypass <player> on/off");
                    return;
                }

            } else {
                sender.sendMessage("§cUnknown command usage");
            }

        }

    }

    public void mapsSubcommand(CommandSender sender) {

        if (!(this.plugin.getGame() instanceof Lobby)) {
            sender.sendMessage("§cNo lobby running");
            return;
        }

        MapData mapData = ((Lobby) this.plugin.getGame()).getSelectedMap();

        if (mapData == null) {
            sender.sendMessage("§7No map selected");
        } else {
            sender.sendMessage("§7Selected map: " + mapData.getName() + " (" + mapData.getId() + ")");
        }

        sender.sendMessage("§7Available Maps:");

        for (MapData map : List.copyOf(((Lobby) this.plugin.getGame()).getMaps())) {
            sender.sendMessage("§7" + map.getName() + " (" + map.getId() + ")");
        }

    }

    public void forcemapSubcommand(CommandSender sender, String[] args) {

        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage("§cNo permission");
            return;
        }

        if (!(this.plugin.getGame() instanceof Lobby)) {
            sender.sendMessage("§cNo lobby running");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /combattest forcemap <mapName/id:zoneId>");
            return;
        }

        String mapName = args[1];

        for (int i = 2; i < args.length; i++) {
            mapName = mapName + " " + args[i];
        }

        MapData mapData = this.getMapFromString(mapName);

        if (mapData == null) {
            sender.sendMessage("§cMap does not exist");
            return;
        }

        ((Lobby) this.plugin.getGame()).selectMap(mapData);
        sender.sendMessage("§aMap successfully selected");

    }

    public void votemapCommand(CommandSender sender, String[] args) {

        if (!(this.plugin.getGame() instanceof Lobby)) {
            sender.sendMessage("§cNo lobby running");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThe command needs to be executed by a player");
            return;
        }

        LobbyPlayerData playerData = ((Lobby) this.plugin.getGame()).getPlayers().get(((Player) sender).getUniqueId());

        if (playerData == null) {
            sender.sendMessage("§cYou are not in the lobby");
            return;
        }

        if (!((Lobby) this.plugin.getGame()).isMapVoting() || ((Lobby) this.plugin.getGame()).getSelectedMap() != null) {
            sender.sendMessage("§cMap voting is already over");
            return;
        }

        if (args.length < 2) {
            playerData.setVote(null);
            sender.sendMessage("§aYou successfully removed your vote");
            return;
        }

        String mapName = args[1];

        for (int i = 2; i < args.length; i++) {

            mapName = mapName + " " + args[i];

        }

        MapData mapData = null;

        for (MapData map : ((Lobby) this.plugin.getGame()).getMaps()) {

            if (map.getName().equals(mapName)) {
                mapData = map;
                break;
            }

        }

        if (mapData == null) {
            sender.sendMessage("§cMap does not exist");
            return;
        }

        playerData.setVote(mapData);
        sender.sendMessage("§aYou voted for " + mapData.getName());

    }

    public void playerSubcommand(CommandSender sender, String[] args) {

        if (!this.hasAdminPermission(sender)) {
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /cloudacm2 players add/remove/list/get/set");
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {

            if (args.length < 3) {
                sender.sendMessage("§c/cloudacm2 players add <uuid/name>");
                return;
            }

            if (!(this.plugin.getGame() instanceof Lobby)) {
                sender.sendMessage("§cA lobby must be running to add players\n§cTo add players during a running game, you need to use the scoreboard objectives");
                return;
            }

            UUID playerId = this.plugin.getPlayerUUIDFromString(args[2]);

            if (playerId == null) {
                sender.sendMessage("§cPlayer not found");
                return;
            }

            ((Lobby) this.plugin.getGame()).addPlayer(playerId);
            sender.sendMessage("§aPlayer successfully added");

            return;
        }

        if (args[1].equalsIgnoreCase("remove")) {

            if (args.length < 3) {
                sender.sendMessage("§c/cloudacm2 players remove <uuid/name>");
                return;
            }

            if (!(this.plugin.getGame() instanceof Lobby)) {
                sender.sendMessage("§cA lobby must be running to remove players\n§cTo remove players during a running game, you need to use the scoreboard objectives");
                return;
            }

            UUID playerId = this.plugin.getPlayerUUIDFromString(args[2]);

            if (playerId == null) {
                sender.sendMessage("§cPlayer not found");
                return;
            }

            ((Lobby) this.plugin.getGame()).removePlayer(playerId);
            sender.sendMessage("§aPlayer successfully removed");

            return;
        }

        if (args[1].equalsIgnoreCase("list")) {

            if (!(this.plugin.getGame() instanceof Lobby)) {
                sender.sendMessage("§cA lobby must be running to list players\n§cTo list players during a running game, you need to look into the tablist or use the scoreboard objectives");
                return;
            }

            sender.sendMessage("§7PLAYER LIST:");

            for (UUID playerId : ((Lobby) this.plugin.getGame()).getPlayers().keySet()) {
                Player player = this.plugin.getServer().getPlayer(playerId);

                if (player == null) {
                    sender.sendMessage("§7" + playerId + " [OFFLINE]");
                    continue;
                }

                sender.sendMessage("§7" + player.getName() + " (" + player.getUniqueId() + ")");

            }

            return;
        }

        if (args[1].equalsIgnoreCase("get")) {

            if (!(this.plugin.getGame() instanceof Lobby)) {
                sender.sendMessage("§cA lobby must be running to get player information");
                return;
            }

            if (args.length < 4) {
                sender.sendMessage("§cUsage: /cloudacm2 players get <uuid/player> vote/team");
                return;
            }

            UUID playerId = this.plugin.getPlayerUUIDFromString(args[2]);

            if (playerId == null) {
                sender.sendMessage("§cPlayer not found");
                return;
            }

            LobbyPlayerData playerData = ((Lobby) this.plugin.getGame()).getPlayers().get(playerId);

            if (playerData == null) {
                sender.sendMessage("§cPlayer not in lobby");
                return;
            }

            switch (args[3]) {
                case "vote" -> {
                    if (playerData.getVote() != null) {
                        sender.sendMessage("§7Vote: " + playerData.getVote().getName() + " (" + playerData.getVote().getId() + ")");
                    } else {
                        sender.sendMessage("§7Vote: ---");
                    }
                }
                case "team" -> sender.sendMessage("§7Team: " + playerData.getTeam());
                default -> sender.sendMessage("§cValue not found");
            }

            return;
        }

        if (args[1].equalsIgnoreCase("set")) {

            if (!(this.plugin.getGame() instanceof Lobby)) {
                sender.sendMessage("§cA lobby must be running to get player information");
                return;
            }

            if (args.length < 5) {
                sender.sendMessage("§cUsage: /cloudacm2 players set <uuid/player> vote/team <value>");
                return;
            }

            UUID playerId = this.plugin.getPlayerUUIDFromString(args[2]);

            if (playerId == null) {
                sender.sendMessage("§cPlayer not found");
                return;
            }

            LobbyPlayerData playerData = ((Lobby) this.plugin.getGame()).getPlayers().get(playerId);

            if (playerData == null) {
                sender.sendMessage("§cPlayer not in lobby");
                return;
            }

            switch (args[3]) {
                case "vote" -> {
                    if (args[4].equalsIgnoreCase("null")) {
                        sender.sendMessage("§aCleared vote");
                    } else {

                        String mapName = args[4];

                        for (int i = 2; i < args.length; i++) {
                            mapName = mapName + " " + args[i];
                        }

                        MapData mapData = this.getMapFromString(mapName);

                        if (mapData == null) {
                            sender.sendMessage("§cMap does not exist");
                            return;
                        }

                        playerData.setVote(mapData);

                    }
                }
                case "team" -> {

                    try {
                        playerData.setTeam(Integer.parseInt(args[4]));
                        sender.sendMessage("§aTeam set");
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage("§cPlease specify a valid int value");
                        return;
                    }

                }
                default -> sender.sendMessage("§cValue not found");
            }

            return;
        }

    }

    public void cloudsystemModeSubcommand(CommandSender sender, String[] args) {

        if (!this.hasAdminPermission(sender)) {
            sender.sendMessage("§cNo permission");
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("disable")) {

            this.plugin.setCloudSystemMode(false);
            sender.sendMessage("§aCloudSystem mode disabled. To re-enable it, restart the server.");

        } else {

            sender.sendMessage("§7CloudSystem mode: " + this.plugin.isCloudSystemMode());
            sender.sendMessage("§7To disable it, use /bedwars cloudsystemmode disable (restart for re-enabling required).");

        }

    }

    private MapData getMapFromString(String mapName) {

        MapData mapData = null;

        for (MapData map : List.copyOf(((Lobby) this.plugin.getGame()).getMaps())) {

            if (mapName.startsWith("id:")) {

                if (String.valueOf(map.getId()).equals(mapName.substring(3))) {
                    mapData = map;
                }

            } else {

                if (map.getName().equals(mapName)) {
                    mapData = map;
                }

            }

        }

        return mapData;
    }

    public boolean hasAdminPermission(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission("cloudacm2.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        return List.of();
    }
}
