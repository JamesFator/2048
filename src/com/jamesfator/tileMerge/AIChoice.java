package com.jamesfator.tileMerge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AIChoice extends ChoiceAlgorithm {

    static int nMoves = 4;
    float[] hypothesis;
    ArrayList<float[]> history;
    ArrayList<Float> depth;
    int minSearchTime = 5;
    
    public AIChoice(float[] hypo) {
        super(false);
        // copy the hypothesis
        hypothesis = Arrays.copyOf(hypo, hypo.length);
        history = new ArrayList<float[]>();
        depth = new ArrayList<Float>();
    }
    
    public AIChoice(boolean debugMode) {
        super(debugMode);
        hypothesis = new float[] {0.1f, 0.1f, 0.1f, 0.1f, 0.1f};
        history = new ArrayList<float[]>();
        depth = new ArrayList<Float>();
    }
    
    public float[] getHypothesis() {
        return hypothesis;
    }
    
    public void setHypothesis(float[] newHypothesis) {
        hypothesis = Arrays.copyOf(newHypothesis, newHypothesis.length);
    }
    
    public void reset() {
        history = new ArrayList<float[]>();
        depth = new ArrayList<Float>();
    }

    public String toString() {
        return "AI Score";
    }
    
    public static String prettyHypothesis(float[] hypothesis) {
        String res = "{";
        for (int i = 0; i < hypothesis.length; i++) {
            res += hypothesis[i] + "f";
            if (i != hypothesis.length-1)
                res += ", ";
        }
        res += "}";
        return res;
    }
    
    /**
     * 
     * @param depth
     * @param alpha
     * @param beta
     * @param positions
     * @param cutoffs
     * @return float[(int)move, score, (int)position, (int)cutoffs]
     */
    private float[] search(final Grid gridD, int depth, float alpha, float beta,
            int positions, int cutoffs) {
        float bestScore;
        int bestMove = -1;
        float[] result;
        
        if (gridD.playerTurn) {
            // The maximizing player
            bestScore = alpha;
            for (int dir = 0; dir < nMoves; dir++) {
                Grid newGrid = new Grid(gridD);
                int val = newGrid.shiftBoard(dir);
                if (val != -1) {
                    positions++;
                    if (newGrid.won)
                        return new float[] {dir, 10000, positions, cutoffs};
                    
                    if (depth == 0) {
                        float[] features = newGrid.getFeatures();
                        float eval = LearningSystem.evaluateGrid(
                                features, this.hypothesis);
                        result = new float[] {dir, eval, positions, cutoffs};
                    } else {
                        result = this.search(newGrid, depth-1, bestScore,
                                beta, positions, cutoffs);
                        if (result[1] > 9900) {
                            // slightly penalize higher depth from win
                            result[1]--;
                        }
                        positions = (int)result[2];
                        cutoffs = (int)result[3];
                    }
                    
                    if (result[1] > bestScore) {
                        bestScore = result[1];
                        bestMove = dir;
                    }
                    if (bestScore > beta) {
                        cutoffs++;
                        return new float[]{bestMove,beta,positions,cutoffs};
                    }
                }
            }
        } else {
            // Computers turn
            bestScore = beta;
            
            // Try 2 and 4 in each cell and measure maximum damage
            //   with board evaluation
            ArrayList<Tile> candidates = new ArrayList<Tile>();
            ArrayList<Tile> cells = gridD.availableCells();
            ArrayList<int[]> scores = new ArrayList<int[]>();
            scores.add(new int[cells.size()]);  // Value 2 Tiles
            scores.add(new int[cells.size()]);  // Value 4 Tiles
            for (int value = 0; value < 2; value++) {
                for (int i = 0; i < cells.size(); i++) {
                    Tile t = cells.get(i);
                    scores.get(value);
                    gridD.setCell(t.x, t.y, (value+1)); // Insert tile
                    scores.get(value)[i] = gridD.betaEval();
                    gridD.setCell(t.x, t.y, 0);  // Remove tile
                }
            }
            
            // Remove the most damage inflicting tiles
            int maxScore = 0;
            for (int value = 0; value < 2; value++) {
                for (int score : scores.get(value)) {
                    if (score > maxScore)
                        maxScore = score;
                }
            }
            for (int value = 0; value < 2; value++) {
                for (int i = 0; i < scores.get(value).length; i++) {
                    if (scores.get(value)[i] == maxScore) {
                        Tile t = cells.get(i);
                        candidates.add(new Tile(t.x, t.y, (value+1)));
                    }
                }
            }
            
            // Search on each candidate
            for (int i = 0; i < candidates.size(); i++) {
                Tile t = candidates.get(i);
                Grid newGrid = new Grid(gridD);
                newGrid.setCell(t);
                newGrid.playerTurn = true;
                positions++;
                result = this.search(newGrid, depth, alpha, bestScore,
                        positions, cutoffs);
                positions = (int)result[2];
                cutoffs = (int)result[3];
                
                if (result[1] < bestScore) {
                    bestScore = result[1];
                }
                if (bestScore < alpha) {
                    cutoffs++;
                    return new float[]{-1,alpha,positions,cutoffs};
                }
            }
        }

        return new float[] {bestMove, bestScore, positions, cutoffs};
    }
    
    private float ply1Helper(Grid grid) {
        float evalScore = 0;
        
        // Try 2 and 4 in each cell and measure maximum damage
        //   with board evaluation
        ArrayList<Tile> candidates = new ArrayList<Tile>();
        ArrayList<Tile> cells = grid.availableCells();
        ArrayList<int[]> scores = new ArrayList<int[]>();
        scores.add(new int[cells.size()]);  // Value 2 Tiles
        scores.add(new int[cells.size()]);  // Value 4 Tiles
        for (int value = 0; value < 2; value++) {
            for (int i = 0; i < cells.size(); i++) {
                Tile t = cells.get(i);
                scores.get(value);
                grid.setCell(t.x, t.y, (value+1)); // Insert tile
                scores.get(value)[i] = grid.betaEval();
                grid.setCell(t.x, t.y, 0);  // Remove tile
            }
        }
        
        // Remove the most damage inflicting tiles
        int maxScore = 0;
        for (int value = 0; value < 2; value++) {
            for (int score : scores.get(value)) {
                if (score > maxScore)
                    maxScore = score;
            }
        }
        for (int value = 0; value < 2; value++) {
            for (int i = 0; i < scores.get(value).length; i++) {
                if (scores.get(value)[i] == maxScore) {
                    Tile t = cells.get(i);
                    candidates.add(new Tile(t.x, t.y, (value+1)));
                }
            }
        }
        
        // Search on each candidate
        for (int i = 0; i < candidates.size(); i++) {
            Tile t = candidates.get(i);
            grid.setCell(t);
            grid.playerTurn = true;
            if (grid.isMoveAvailable()) {
                for (int dir = 0; dir < nMoves; dir++) {
                    Grid newGrid = new Grid(grid);
                    int val = newGrid.shiftBoard(dir);
                    if (val != -1 && newGrid.isMoveAvailable()) {
                        if (newGrid.won)
                            return 100;
                        // Evaluate and merge scores
                        evalScore += LearningSystem.evaluateGrid(
                                newGrid.getFeatures(), this.hypothesis);
                    }
                }
            }
            // remove the cell
            t.value = 0; grid.setCell(t);
        }
        return evalScore;
    }

    public int makeChoice() {
//        int chosenDirection = -1;
//        float bestScore = Float.NEGATIVE_INFINITY;
//        for (int dir = 0; dir < nMoves; dir++) {
//            Grid newGrid = new Grid(tm.grid);
//            int val = newGrid.shiftBoard(dir);
//            if (val != -1) {
//                if (newGrid.won) {
//                    chosenDirection = dir;
//                    break;
//                }
//                float gridScore = LearningSystem.evaluateGrid(
//                        newGrid.getFeatures(), this.hypothesis);
//                if (gridScore > bestScore) {
//                    bestScore = gridScore;
//                    chosenDirection = dir;
//                }
//            }
//        }
        
        int chosenDirection = -1;
        int depth = 0;
        long start = System.currentTimeMillis();
        float bestScore = Float.NEGATIVE_INFINITY;
        do {
            float[] newBest = this.search(super.tm.grid, depth, -10000, 10000, 0, 0);
            if (newBest[0] == -1)
                break; // break early
            chosenDirection = (int)newBest[0];
            if (newBest[1] > bestScore)
                bestScore = newBest[1];
            depth++;
        } while(System.currentTimeMillis() - start < minSearchTime);
        
        // Add this configuration to the history
//        this.history.add(tm.grid.getFeatures());
//        this.depth.add((float)depth);
        
        if (chosenDirection == -1) {
            System.err.println("BAD CHOICE ALERT!!!");
            chosenDirection = (new Random()).nextInt(4);
        }
        
        return chosenDirection;
    }
    
    public ChoiceAlgorithm newSolver() {
        AIChoice newAlgo = new AIChoice(debug);
        newAlgo.setHypothesis(this.hypothesis);
        newAlgo.minSearchTime = this.minSearchTime;
        return newAlgo;
    }
}
