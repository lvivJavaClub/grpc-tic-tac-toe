package org.javaclub.grpc.tictactoeserver;

public class Game {

    private String playerX;
    private String playerO;

    public boolean newPlayer(String id) {
        if (playerX == null) {
            playerX = id;
            return true;
        }
        if (playerO == null) {
            playerO = id;
            return true;
        }
        return false;
    }

    public Character makeMove(String id) {
        if (id.equals(playerX)) {
            return Character.X;
        }
        if (id.equals(playerO)) {
            return Character.O;
        }
        return Character.UNRECOGNIZED;
    }
}
