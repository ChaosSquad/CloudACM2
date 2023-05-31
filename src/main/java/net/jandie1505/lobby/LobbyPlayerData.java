package net.jandie1505.lobby;

public class LobbyPlayerData {
    private int vote;
    private int team;

    public LobbyPlayerData() {
        this.vote = 0;
        this.team = 0;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }
}
