package net.jandie1505.cloudacm2.game;

import net.jandie1505.cloudacm2.CloudACM2;
import net.jandie1505.cloudacm2.GamePart;
import net.jandie1505.cloudacm2.lobby.LobbyPlayerData;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Game implements GamePart {
    private final CloudACM2 plugin;
    private boolean killswitch;
    private final World world;
    private final int gamemode;
    private final int selectedMap;
    private final Map<UUID, PlayerData> players;

    public Game(CloudACM2 plugin, World world, int gamemode, int selectedMap, Map<UUID, LobbyPlayerData> players) {
        this.plugin = plugin;
        this.killswitch = false;
        this.world = world;
        this.gamemode = gamemode;
        this.selectedMap = selectedMap;
        this.players = new HashMap<>();

        if (world == null) {
            return;
        }

        for (UUID playerId : players.keySet()) {
            LobbyPlayerData lobbyPlayerData = players.get(playerId);
            Player player = this.plugin.getServer().getPlayer(playerId);

            if (player == null || lobbyPlayerData == null) {
                continue;
            }

            PlayerData playerData = new PlayerData();

            if (lobbyPlayerData.getTeam() >= 1 && lobbyPlayerData.getTeam() <= 2) {
                playerData.setTeam(lobbyPlayerData.getTeam());
            }

            this.players.put(playerId, playerData);
        }
    }

    @Override
    public boolean tick() {

        // Stop game

        if (this.killswitch || this.world == null) {
            return false;
        }

        return true;
    }

    @Override
    public GamePart getNextStatus() {
        return null;
    }

    public World getWorld() {
        return this.world;
    }
}
