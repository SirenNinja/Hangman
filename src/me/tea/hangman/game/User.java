package me.tea.hangman.game;

public class User {

    private int wins;
    private int loses;
    private int games;

    public User(){
        wins = 0;
        loses = 0;
        games = 0;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        this.wins = (this.wins + 1);
    }

    public int getLoses() {
        return loses;
    }

    public void addLose() {
        this.loses = (this.loses + 1);
    }

    public int getGames() {
        return games;
    }

    public void addGame() {
        this.games = (this.games + 1);
    }
}
