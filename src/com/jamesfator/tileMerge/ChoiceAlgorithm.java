package com.jamesfator.tileMerge;

/**
 * ChoiceAlgorithm defaults to a random choice
 * @author James Fator
 */
public abstract class ChoiceAlgorithm {
    
    public boolean debug;
    TileMerge tm;
    
    public ChoiceAlgorithm(boolean debugMode) {
        this.debug = debugMode;
    }
    
    public void setTileMerge(TileMerge tmGame) {
        this.tm = tmGame;
    }
    
    public void reset() {}
    
    public abstract int makeChoice();
    
    public abstract ChoiceAlgorithm newSolver();
    
    public void debugState(int direction) {
        String dir = "";
        if (direction == 0) dir = "up";
        if (direction == 1) dir = "right";
        if (direction == 2) dir = "down";
        if (direction == 3) dir = "left";
        System.out.println("\nmoved => "+dir);
        System.out.println(tm.score);
        System.out.println(tm.grid.prettyString());
        System.out.println("\n");
    }
    
    public abstract String toString();
}