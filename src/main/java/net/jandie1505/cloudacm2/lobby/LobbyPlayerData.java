package net.jandie1505.cloudacm2.lobby;

public class LobbyPlayerData {
    private MapData vote;
    private int team;

    public LobbyPlayerData() {
        this.vote = null;
        this.team = 0;
    }

    public MapData getVote() {
        return vote;
    }

    public void setVote(MapData vote) {
        this.vote = vote;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }
}
