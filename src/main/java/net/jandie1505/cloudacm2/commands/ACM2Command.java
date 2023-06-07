package net.jandie1505.cloudacm2.commands;

import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.game.Game;
import net.jandie1505.cloudacm2.lobby.Lobby;
import org.bukkit.command.*;

import java.util.List;

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

    public boolean hasAdminPermission(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission("cloudacm2.admin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        return List.of();
    }
}
