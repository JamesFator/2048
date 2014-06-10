package com.jamesfator.tileMerge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TileMergeTrainer {
    
    public static void multiThreadSim(int nTrials, ChoiceAlgorithm algo)
            throws InterruptedException, ExecutionException {
        // Divide by number of processors
        int nThreads = Runtime.getRuntime().availableProcessors();
        int nPer = Math.round((float)nTrials/nThreads);
        System.out.println("Trials: " + nThreads*nPer);
        // Arrays to hold the games
        ExecutorService execSvc = Executors.newCachedThreadPool();
        ArrayList<TrainSpawn> spawns = new ArrayList<TrainSpawn>();
        // Spawn the new threads
        for (int i = 0; i < nThreads; i++) {
            TileMerge tm = new TileMerge(new Random(), algo.newSolver());
            TrainSpawn gs = new TrainSpawn(tm, nPer);
            spawns.add(gs);
        }
        List<Future<float[]>> results = execSvc.invokeAll(spawns);
        for (Future<float[]> returned : results) {
            float[] hypothesis = returned.get();
            for (float f : hypothesis) {
                System.out.print(f);
                System.out.print("f, ");
            }
            System.out.println();
        }
        execSvc.shutdownNow();
    }

    public static void main(String[] args) {
        float[] hypothesis = new float[] {0.5f, 0.5f, 0.5f, -0.5f, -0.5f};
//        hypothesis = new float[] {-0.3150543f, -5.901716f, 14.477651f, 22.604965f, 7.1315174f};
        
        EvaluatorChoice choiceAlgo = new EvaluatorChoice(false);
        choiceAlgo.setHypothesis(hypothesis);
        TileMerge tm = new TileMerge(new Random(Long.parseLong("1395609528254")), choiceAlgo);
        tm.runSimulation();
        ArrayList<float[]> history = choiceAlgo.history;
        ArrayList<Float> trainData =
                LearningSystem.getTrainingExamples(hypothesis, history);
        hypothesis = LearningSystem.updateWeights(hypothesis,
                history, trainData);
        choiceAlgo.setHypothesis(hypothesis);
        tm.runSimulation();
        history = choiceAlgo.history;
        trainData = LearningSystem.getTrainingExamples(hypothesis, history);
        hypothesis = LearningSystem.updateWeights(hypothesis,
                history, trainData);
        System.out.println(tm.grid);
        choiceAlgo.setHypothesis(hypothesis);
        
        try {
            int nTrials = 100000;
//            EvaluatorChoice choiceAlgo = new EvaluatorChoice(false);
            choiceAlgo.setHypothesis(hypothesis);
            TileMergeTrainer.multiThreadSim(nTrials, choiceAlgo);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}

class TrainSpawn implements Callable<float[]> {
    
    public TileMerge game;
    private int numTests;
    
    public TrainSpawn(TileMerge tm, int numTests) {
        this.game = tm;
        this.numTests = numTests;
    }

    @Override
    public float[] call() throws Exception {
        EvaluatorChoice algo = (EvaluatorChoice)game.algo;
        float[] hypothesis = algo.hypothesis;
        for (int i = 0; i < numTests; i++) {
            game.runSimulation();
            if (game.grid.won) {
                ArrayList<float[]> history = algo.history;
                ArrayList<Float> trainData =
                        LearningSystem.getTrainingExamples(hypothesis, history);
                hypothesis = LearningSystem.updateWeights(hypothesis,
                        history, trainData);
                algo.setHypothesis(hypothesis);
            }
        }
        return hypothesis;
    }
}

class TrainData {
    
    public ArrayList<int[]> history;
    public ArrayList<Float> trainData;
    
    public TrainData(ArrayList<int[]> history, ArrayList<Float> examples) {
        this.history = history;
        this.trainData = examples;
    }
}
