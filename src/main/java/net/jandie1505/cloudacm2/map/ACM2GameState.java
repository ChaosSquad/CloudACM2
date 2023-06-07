package net.jandie1505.cloudacm2.map;

public final class ACM2GameState {
    public static final int NONE = 0;
    public static final int START_LOBBY = 1;
    public static final int LOBBY = 2;
    public static final int START_GAME = 3;
    public static final int GAME = 4;
    public static final int START_ENDLOBBY = 5;
    public static final int ENDLOBBY = 6;
    public static final int PREPARE_RESET = 7;
    public static final int RESET = 8;

    private ACM2GameState() {}
}
