package org.javaclub.grpc.tictactoeserver;

import static org.javaclub.grpc.tictactoeserver.Character.EMPTY;

public class Game {

    private String playerX;
    private String playerO;
    private Character[][] board = {
            {EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY},
            {EMPTY, EMPTY, EMPTY}
    };

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

    public Character makeMove(String id, Point point) {
        Character character = Character.UNRECOGNIZED;
        if (board[point.getX()][point.getY()] != Character.EMPTY) {
            return Character.UNRECOGNIZED;
        }
        if (id.equals(playerX)) {
            character = Character.X;
        }
        if (id.equals(playerO)) {
            character = Character.O;
        }
        board[point.getX()][point.getY()] = character;
        return character;
    }

    public boolean isFinished() {
        for (Character[] aBoard : board) {
            for (int j = 0; j < board.length; j++) {
                if (aBoard[j] == Character.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    public void reset() {
        for (Character[] aBoard : board) {
            for (int j = 0; j < board.length; j++) {
                aBoard[j] = Character.EMPTY;
            }
        }
        playerO = null;
        playerX = null;
    }
}
