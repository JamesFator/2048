package com.jamesfator.tileMerge;

import java.util.ArrayList;

public class EvaluatorChoice extends ChoiceAlgorithm {
    
    float[] hypothesis;
    ArrayList<float[]> history;
    
    public EvaluatorChoice(boolean debugMode) {
        super(debugMode);
        hypothesis = new float[] {0.5f, 0.5f, 0.5f, 0.5f, 0.5f};
        history = new ArrayList<float[]>();
    }
    
    public float[] getHypothesis() {
        return hypothesis;
    }
    
    public void setHypothesis(float[] newHypothesis) {
        hypothesis = new float[newHypothesis.length];
        for (int n = 1; n < newHypothesis.length; n++)
            hypothesis[n] = newHypothesis[n];
    }
    
    public void reset() {
        history = new ArrayList<float[]>();
    }

    public String toString() {
        return "Evaluator Score";
    }

    public int makeChoice() {
        int chosenDirection = -1;
        float[] chosenFeatures = null;
        Grid gridCache = new Grid(tm.grid);
        int scoreCache = new Integer(tm.score);
        float bestScore = -1337;
        for (int direction = 0; direction < 4; direction++) {
            int scoreDiff = tm.grid.shiftBoard(direction);
            float[] features = tm.grid.getFeatures();
            float score = LearningSystem.evaluateGrid(features, hypothesis);
            if ((bestScore == -1337 || score > bestScore) && scoreDiff != -1) {
                bestScore = score;
                chosenDirection = direction;
                chosenFeatures = features;
            } else if (bestScore == -1337 && scoreDiff == -1) {
                // Add the option if the board is different.
                chosenDirection = direction;
                chosenFeatures = features;
            }
            // Restore the game
            tm.score = new Integer(scoreCache);
            tm.grid = new Grid(gridCache);
        }
        
        // Add this next move to the history
        history.add(chosenFeatures);
        return chosenDirection;
    }
    
    public ChoiceAlgorithm newSolver() {
        EvaluatorChoice newAlgo = new EvaluatorChoice(debug);
        newAlgo.setHypothesis(this.hypothesis);
        return newAlgo;
    }
}
