package com.jamesfator.tileMerge;

public class RandomChoice extends ChoiceAlgorithm {
    
    public RandomChoice(boolean debugMode) {
        super(debugMode);
    }

    public String toString() {
        return "Random Choice";
    }

    public int makeChoice() {
        return tm.rand.nextInt(4);
    }
    
    public ChoiceAlgorithm newSolver() {
        return new RandomChoice(debug);
    }
}
