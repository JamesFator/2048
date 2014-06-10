package com.jamesfator.tileMerge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TileMerge {
    
    int score;
    Grid grid;
    Random rand;
    ChoiceAlgorithm algo;
    
    public TileMerge(Random rand, ChoiceAlgorithm algo) {
        // Create the grid and add initial tiles
        this.grid = new Grid();
        this.score = 0;
        this.rand = rand;
        this.algo = algo;
        this.algo.setTileMerge(this);
        for (int i = 0; i < 2; i ++)
            this.grid.placeRandomCell(this.rand);
    }
    
    public void reset() {
        score = 0;
        grid.reset();
        algo.reset();
        // Place initial cells
        for (int i = 0; i < 2; i ++)
            this.grid.placeRandomCell(this.rand);
    }
    
    public void move(int direction) {
        // Check to see if the game is over
        if (grid.isGameTerminated()) return;
        int pointsGained = grid.shiftBoard(direction);
        if (pointsGained != -1) {
            // Movement of tiles, add the points if any
            score += pointsGained;
            grid.placeRandomCell(rand);
        }
        // Debug if necessary
        if (algo.debug) algo.debugState(direction);
    }
    
    public void runSimulation() {
        reset();
        while (grid.isGameTerminated() == false) {
            int direction = algo.makeChoice();
            move(direction);
        }
    }
    
    public int numWins(int numTests) {
        int count = 0;
        for (int i = 0; i < numTests; i++) {
            this.runSimulation();
            if (this.grid.won) count++;
        }
        System.out.println(System.currentTimeMillis() +
                " Completed trial: " + count);
        return count;
    }
    
    public int medianScore(int numTests) {
        int[] scores = new int[numTests];
        for (int i = 0; i < numTests; i++) {
            this.runSimulation();
            scores[i] = this.score;
        }
        Arrays.sort(scores);
        return scores[scores.length/2];
    }
    
    public int averageScore(int numTests) {
        int[] scores = new int[numTests];
        for (int i = 0; i < numTests; i++) {
            this.runSimulation();
            scores[i] = this.score;
        }
        int scoresTotal = 0;
        for (int score : scores)
            scoresTotal += score;
        return scoresTotal / numTests;
    }
    
    /**
     * 
     * @param nTrials
     * @param algo
     * @return float[][] {{winRatio},{averageScore},{timePerGame}}
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static float[] multiThreadSim(ChoiceAlgorithm algo, int nTrials)
            throws InterruptedException, ExecutionException {
        // Divide by number of processors
        int nThreads = Runtime.getRuntime().availableProcessors();
        int nPer = Math.round((float)nTrials/nThreads);
        int spawns = nThreads * nPer;
        // Arrays to hold the games
        ExecutorService execSvc = Executors.newCachedThreadPool();
        ArrayList<GameSpawn> gameSpawns = new ArrayList<GameSpawn>();
        for (int i = 0; i < nThreads; i++) {
            TileMerge tm = new TileMerge(new Random(), algo.newSolver());
            GameSpawn gs = new GameSpawn(tm, nPer);
            gameSpawns.add(gs);
        }
        List<Future<int[][]>> results;
        results = execSvc.invokeAll(gameSpawns);
        int scoresTotal = 0;
        int winCount = 0;
        long durSum = 0;
        for (Future<int[][]> returned : results) {
            int[][] scoreSet;
            scoreSet = returned.get();
            for (int score : scoreSet[0])
                scoresTotal += score;
            winCount += scoreSet[1][0];
            durSum += scoreSet[2][0];
        }
        execSvc.shutdownNow();
        float duration = durSum/(long)nThreads;
        float avgScore = scoresTotal / (float)spawns;
        float winRatio = (float)winCount / (float)spawns;
        return new float[] {winRatio, avgScore, duration};
    }
    
    public static void testSimulation(TileMerge tm, int numTests) {
        long startTime = System.currentTimeMillis();
        int bestScore = 0;
        int scoresTotal = 0;
        for (int i = 0; i < numTests; i++) {
            tm.runSimulation();
            if (tm.score > bestScore)
                bestScore = tm.score;
            scoresTotal += tm.score;
        }
        float avgScore = scoresTotal / (float)numTests;
        long duration = System.currentTimeMillis() - startTime;
        System.out.println(tm.algo + ":\n\tbest: " + bestScore +
                "\n\tavg:  " + avgScore + "\n\ttime: " + duration);
        System.out.println(bestScore);
        System.out.println(tm.grid.prettyString());
    }
    
    public static void monteCarlo(TileMerge tm, int nTests) {
        AIChoice algo = (AIChoice)tm.algo;
        for (int n : new int[] {10}) {
            algo.minSearchTime = n;
            try {
                float[] res = multiThreadSim(algo, nTests);
                float winRatio = res[0];
                float avgScore = res[1];
                float duration = res[2];
                System.out.println("  N: " + n +
                        "\n\twin:  " + winRatio +
                        "\n\tavg:  " + avgScore +
                        "\n\ttime: " + duration);
                System.out.println("\n\n");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                break;
            }
        }
    }
    
    public static void testAI() {
        long seed = System.currentTimeMillis();
//        seed = Long.parseLong("1396603661179");
        System.out.println("Seed: " + seed);
        Random rand = new Random(seed);
        AIChoice choiceAlgo = new AIChoice(false);
        choiceAlgo.setHypothesis(new float[] {0.5f, 0.5f, 0.5f, 0.5f, 0.5f});
        TileMerge tm = new TileMerge(rand, choiceAlgo);
        monteCarlo(tm, 16);
    }
    
    public static void temporalDifference() {
        long seed = System.currentTimeMillis();
        //seed = Long.parseLong("1395931644968");
        System.out.println("Seed: " + seed);
        Random rand = new Random(seed);
        AIChoice choiceAlgo = new AIChoice(false);
        choiceAlgo.setHypothesis(new float[] {0.1f, 0.1f, 0.1f, 0.1f, 0.1f});
        int wins = 0;
        int loses = 0;
        while (true) {
            TileMerge tm = new TileMerge(rand, choiceAlgo);
            tm.runSimulation();
            float[] hypo = LearningSystem.updateWeights2(choiceAlgo, tm.grid.won);
            choiceAlgo.setHypothesis(hypo);
            if (tm.grid.won) wins++;
            else loses++;
            System.out.println(wins + "/" + loses + " => " +
                    AIChoice.prettyHypothesis(hypo) + " with " + tm.score);
        }
    }
    
    public static void genetic() {
        try {
            System.out.println(System.currentTimeMillis() + " Starting\n");
            Subject res = LearningSystem.evolveHeuristics();
            System.out.println(System.currentTimeMillis() + " Result:\n\t");
            System.out.print(AIChoice.prettyHypothesis(res.hypothesis));
            System.out.println(" => " + res.score);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    
    public static void main(String[] args) {
//        temporalDifference();
        testAI();
    }
}

class GameSpawn implements Callable<int[][]> {
    
    public TileMerge game;
    private int nTests;
    
    public GameSpawn(TileMerge tm, int nTests) {
        this.game = tm;
        this.nTests = nTests;
    }

    /**
     * @return float[][] {{scores...},{winRatio},{duration}}
     */
    @Override
    public int[][] call() throws Exception {
        long start = System.currentTimeMillis();
        int[][] scores = new int[3][];
        scores[0] = new int[nTests];
        scores[1] = new int[1];
        scores[2] = new int[1];
        int winCount = 0;
        for (int i = 0; i < nTests; i++) {
            game.runSimulation();
            scores[0][i] = game.score;
            if (game.grid.won) winCount++;
        }
        scores[1][0] = winCount;
        scores[2][0] = (int)(System.currentTimeMillis() - start);
        
        return scores;
    }
}
