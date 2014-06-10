package com.jamesfator.tileMerge;

import java.util.ArrayList;

public class HighestScore extends ChoiceAlgorithm {
    
    public HighestScore(boolean debugMode) {
        super(debugMode);
    }

    public String toString() {
        return "Highest Score";
    }

    public int makeChoice() {
        int chosenDirection = 0;
        Grid gridCache = new Grid(tm.grid);
        int scoreCache = new Integer(tm.score);
        int bestScore = 0;
        int leastEmpty = 17;
        ArrayList<Integer> options = new ArrayList<Integer>();
        for (int direction = 0; direction < 4; direction++) {
            int score = tm.grid.shiftBoard(direction);
            int nEmpty = tm.grid.numEmptyCells();
            if (score > bestScore && nEmpty <= leastEmpty) {
                bestScore = score;
                chosenDirection = direction;
                leastEmpty = nEmpty;
            } else if (score != -1) {
                // Add the option if the board is different.
                options.add(direction);
            }
            // Restore the game
            tm.score = new Integer(scoreCache);
            tm.grid = new Grid(gridCache);
        }
        if (bestScore == 0)
            chosenDirection = options.get(tm.rand.nextInt(options.size()));
        if (!gridCache.isEqual(tm.grid)) {
            System.err.println("WTF");
            System.exit(0);
        }
        
        return chosenDirection;
    }
    
    public ChoiceAlgorithm newSolver() {
        return new HighestScore(debug);
    }
}
