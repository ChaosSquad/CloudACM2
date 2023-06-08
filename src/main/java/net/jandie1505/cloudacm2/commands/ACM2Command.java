package net.jandie1505.cloudacm2.commands;

import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
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
            sender.sendMessage("§7LOBBY");
        } else if (this.plugin.getGame() instanceof Game) {
            sender.sendMessage("§7INGAME");
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

            sender.sendMessage("§7Your bypass status: " + this.plugin.isPlayerBypassing(((Player) sender).getUniqueId()));

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

                    sender.sendMessage("§7Bypassing status of the player: " + this.plugin.isPlayerBypassing(playerId));

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

        if (mapData == null) {
            sender.sendMessage("§cMap does not exist");
            return;
        }

        ((Lobby) this.plugin.getGame()).selectMap(mapData);
        sender.sendMessage("§aMap successfully selected");

    }

    public boolean hasAdminPermission(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission("cloudacm2.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        return List.of();
    }
}
