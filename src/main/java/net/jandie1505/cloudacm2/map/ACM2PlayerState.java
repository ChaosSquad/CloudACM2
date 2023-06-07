package net.jandie1505.cloudacm2.map;

public final class ACM2PlayerState {
    public static final int DEFAULT = 0;
    public static final int TDM_LOBBY_NO_TEAM = 10;
    public static final int TDM_LOBBY_TEAM_1 = 11;
    public static final int TDM_LOBBY_TEAM_2 = 12;
    public static final int RUSH_LOBBY_NO_TEAM = 16;
    public static final int RUSH_LOBBY_TEAM_1 = 17;
    public static final int RUSH_LOBBY_TEAM_2 = 18;
    public static final int CTF_LOBBY_NO_TEAM = 19;
    public static final int CTF_LOBBY_TEAM_1 = 20;
    public static final int CTF_LOBBY_TEAM_2 = 21;
    public static final int TDM_GAME_TEAM_1 = 30;
    public static final int TDM_GAME_TEAM_2 = 31;
    public static final int RUSH_GAME_TEAM_1 = 34;
    public static final int RUSH_GAME_TEAM_2 = 35;
    public static final int CTF_GAME_TEAM_1 = 36;
    public static final int CTF_GAME_TEAM_2 = 37;
    public static final int TDM_ENDLOBBY_TEAM_1 = 30;
    public static final int TDM_ENDLOBBY_TEAM_2 = 31;
    public static final int RUSH_ENDLOBBY_TEAM_1 = 34;
    public static final int RUSH_ENDLOBBY_TEAM_2 = 35;
    public static final int CTF_ENDLOBBY_TEAM_1 = 36;
    public static final int CTF_ENDLOBBY_TEAM_2 = 37;

    public static int getPlayerScore(int gameState, int gameMode, int team) {

        switch (gameState) {

            case ACM2GameState.START_LOBBY, ACM2GameState.LOBBY -> {

                switch (gameMode) {
                    case ACM2GameMode.RUSH -> {

                        switch (team) {
                            case 1 -> {
                                return RUSH_LOBBY_TEAM_1;
                            }
                            case 2 -> {
                                return RUSH_LOBBY_TEAM_2;
                            }
                            default -> {
                                return RUSH_LOBBY_NO_TEAM;
                            }
                        }

                    }
                    case ACM2GameMode.TDM -> {

                        switch (team) {
                            case 1 -> {
                                return TDM_LOBBY_TEAM_1;
                            }
                            case 2 -> {
                                return TDM_LOBBY_TEAM_2;
                            }
                            default -> {
                                return TDM_LOBBY_NO_TEAM;
                            }
                        }

                    }
                    case ACM2GameMode.CTF -> {

                        switch (team) {
                            case 1 -> {
                                return CTF_LOBBY_TEAM_1;
                            }
                            case 2 -> {
                                return CTF_LOBBY_TEAM_2;
                            }
                            default -> {
                                return CTF_LOBBY_NO_TEAM;
                            }
                        }

                    }
                    default -> {
                        return 0;
                    }
                }

            }
            case ACM2GameState.START_GAME, ACM2GameState.GAME -> {

                switch (gameMode) {
                    case ACM2GameMode.RUSH -> {

                        switch (team) {
                            case 1 -> {
                                return RUSH_GAME_TEAM_1;
                            }
                            case 2 -> {
                                return RUSH_GAME_TEAM_2;
                            }
                            default -> {
                                return 0;
                            }
                        }

                    }
                    case ACM2GameMode.TDM -> {

                        switch (team) {
                            case 1 -> {
                                return TDM_GAME_TEAM_1;
                            }
                            case 2 -> {
                                return TDM_GAME_TEAM_2;
                            }
                            default -> {
                                return 0;
                            }
                        }

                    }
                    case ACM2GameMode.CTF -> {

                        switch (team) {
                            case 1 -> {
                                return CTF_GAME_TEAM_1;
                            }
                            case 2 -> {
                                return CTF_GAME_TEAM_2;
                            }
                            default -> {
                                return 0;
                            }
                        }

                    }
                    default -> {
                        return 0;
                    }
                }

            }
            case ACM2GameState.START_ENDLOBBY, ACM2GameState.ENDLOBBY -> {

                switch (gameMode) {
                    case ACM2GameMode.RUSH -> {

                        switch (team) {
                            case 1 -> {
                                return RUSH_ENDLOBBY_TEAM_1;
                            }
                            case 2 -> {
                                return RUSH_ENDLOBBY_TEAM_2;
                            }
                            default -> {
                                return 0;
                            }
                        }

                    }
                    case ACM2GameMode.TDM -> {

                        switch (team) {
                            case 1 -> {
                                return TDM_ENDLOBBY_TEAM_1;
                            }
                            case 2 -> {
                                return TDM_ENDLOBBY_TEAM_2;
                            }
                            default -> {
                                return 0;
                            }
                        }

                    }
                    case ACM2GameMode.CTF -> {

                        switch (team) {
                            case 1 -> {
                                return CTF_ENDLOBBY_TEAM_1;
                            }
                            case 2 -> {
                                return CTF_ENDLOBBY_TEAM_2;
                            }
                            default -> {
                                return 0;
                            }
                        }

                    }
                    default -> {
                        return 0;
                    }
                }

            }
            default -> {
                return 0;
            }

        }

    }

    private ACM2PlayerState() {}

}
