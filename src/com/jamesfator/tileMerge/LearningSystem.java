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

public class LearningSystem {
    
    static float updateConst = 0.001f;
    static float mutateRate = 0.05f;
    static int numHVars = 4;
    static int numThreads = Runtime.getRuntime().availableProcessors();
    static int numTrials = 100;

    public static float evaluateGrid(float[] features, float[] hypothesis) {
        float score = 0;
        for (int i = 0; i < numHVars; i++) {
            score += features[i] * hypothesis[i];
        }
        return score;
    }
    
    public static ArrayList<Float> getTrainingExamples(float[] hypothesis,
            ArrayList<float[]> history) {
        ArrayList<Float> examples = new ArrayList<Float>();
        
        for (int i = 1; i < history.size(); i++) {
            if (i+1 >= history.size()) {
                float[] finalState = history.get(history.size()-1);
                if (finalState[0] >= Math.log(Grid.goal)/Math.log(2)-1 &&
                        finalState[1] > 0)
                    examples.add(1000.0f);
                else
                    examples.add(-1000.0f);
            } else {
                examples.add(evaluateGrid(history.get(i-1), hypothesis));
            }
        }
        return examples;
    }
    
    public static float[] weightUpdate(AIChoice algo) {
        float[] newHypothesis = Arrays.copyOf(algo.hypothesis,
                algo.hypothesis.length);
        
        return newHypothesis;
    }

    public static float[] updateWeights2(AIChoice algo, boolean won) {
        float[] newHypothesis = Arrays.copyOf(algo.hypothesis,
                algo.hypothesis.length);
        for (int i = 0; i < algo.history.size(); i++) {
            float[] stage = algo.history.get(i);
            float vEst = evaluateGrid(stage, newHypothesis);
            float vTrain;
            if (i == algo.history.size() - 1) {
                vTrain = (won ? 100.0f : -100.0f);
            } else {
                vTrain = evaluateGrid(algo.history.get(i+1), newHypothesis);
            }
            float errorTerm = vTrain - vEst;
            if (Float.isInfinite(errorTerm) || Float.isNaN(errorTerm)) {
                break;
            } else if (Math.abs(errorTerm) > 100) {
                break;
            }
            newHypothesis[0] = updateConst*errorTerm;
            for (int n = 0; n < numHVars; n++) {
                float modifier = updateConst*errorTerm*stage[n];
                newHypothesis[n+1] += modifier;
            }
        }
        return newHypothesis;
    }

    public static float[] updateWeights(float[] hypothesis,
            ArrayList<float[]> history,
            ArrayList<Float> trainingExamples) {
        float[] newHypothesis = new float[hypothesis.length];
        for (int n = 0; n < hypothesis.length; n++)
            newHypothesis[n] = hypothesis[n];
        for (int i = 0; i < trainingExamples.size(); i++) {
            float[] stage = history.get(i);
            float vEst = evaluateGrid(stage, newHypothesis);
            float vTrain = trainingExamples.get(i);

            for (int n = 0; n < hypothesis.length; n++) {
                float change = updateConst*(vTrain - vEst)*stage[n];
                newHypothesis[n] += change;
            }
        }
        return newHypothesis;
    }
    
    private static ArrayList<float[]> randomSubjects(int nHVars,
            int nChildren) {
        ArrayList<float[]> mutatedList = new ArrayList<float[]>();
        Random rand = new Random();
        for (int i = 0; i < nChildren; i++) {
            float[] subj = new float[nHVars];
            for (int n = 0; n < nHVars; n++)
                subj[n] = (rand.nextBoolean() ? 1 : -1) *
                        100.0f * rand.nextFloat();
            mutatedList.add(subj);
        }
        return mutatedList;
    }
    
    private static ArrayList<Subject> fitnessTesting(ArrayList<float[]> pop)
            throws InterruptedException, ExecutionException {
        ExecutorService execSvc = Executors.newCachedThreadPool();
        ArrayList<EvoSpawn> gameSpawns = new ArrayList<EvoSpawn>();
        for (float[] subject : pop) {
            EvoSpawn gs = new EvoSpawn(subject, numTrials);
            gameSpawns.add(gs);
        }
        List<Future<Subject>> results;
        results = execSvc.invokeAll(gameSpawns);
        ArrayList<Subject> tested = new ArrayList<Subject>();
        for (Future<Subject> returned : results)
            tested.add(returned.get());
        execSvc.shutdownNow();
        return tested;
    }
    
    /**
     * selectFittest creates an array of pairs to be mated based on their
     * index in the population ArrayList.
     * @return int[pop.size()][2]
     */
    private static int[][] selectFittest(ArrayList<Subject> pop) {
        int[][] pairArray = new int[pop.size()][2];
        int[] cumulativeFitness = new int[pop.size()];
        cumulativeFitness[0] = pop.get(0).score;
        for (int i = 1; i < pop.size(); i++)
            cumulativeFitness[i] = cumulativeFitness[i-1] + pop.get(i).score;

        Random rand = new Random();
        for (int i = 0; i < pop.size(); i++) {
            int randFitness = rand.nextInt(cumulativeFitness[pop.size()-1]);
            int index = Arrays.binarySearch(cumulativeFitness, randFitness);
            if (index < 0) index = Math.abs(index + 1);
            pairArray[i][0] = index;     // First mate
            int index2;
            do {
                randFitness = rand.nextInt(cumulativeFitness[pop.size()-1]);
                index2 = Arrays.binarySearch(cumulativeFitness, randFitness);
                if (index2 < 0) index2 = Math.abs(index2 + 1);
            } while (index2 == index);   // Prevent mating with one's self
            pairArray[i][1] = index2;    // Second mate
        }
        return pairArray;
    }
    
    private static ArrayList<float[]> mateAndMutate(
            ArrayList<Subject> pop, int[][] mates) {
        ArrayList<float[]> newPopulation = new ArrayList<float[]>();
        
        Random rand = new Random();
        for (int[] mate : mates) {
            Subject m0 = pop.get(mate[0]);
            Subject m1 = pop.get(mate[1]);
            float[] heuristics = Arrays.copyOf(m0.hypothesis, numHVars);
            int p1 = rand.nextInt(numHVars);
            int p2; do { p2 = rand.nextInt(numHVars); } while (p1 == p2);
            // Cross over 2 heuristic variables
            heuristics[p1] = m1.hypothesis[p1];
            heuristics[p2] = m1.hypothesis[p2];
            // Mutate heuristics
            for (int i = 0; i < numHVars; i++)
                if (rand.nextFloat() < mutateRate)
                    heuristics[i] = (rand.nextBoolean()?1:-1) *
                            100.0f * rand.nextFloat();
            // Add the new hypothesis to the population
            newPopulation.add(heuristics);
        }
        
        return newPopulation;
    }
    
    public static Subject evolveHeuristics() {
        // Generate the initial population
        ArrayList<float[]> initPop = randomSubjects(numHVars, numThreads);
        
        for (int i = 0; i < 100000; i++) {
            // Fitness testing
            ArrayList<Subject> tested;
            try {
                tested = fitnessTesting(initPop);
            } catch (Exception e) { e.printStackTrace(); return null; }
            
            // Selection
            int[][] selectedMates = selectFittest(tested);
            
            // Crossover and mutation
            initPop = mateAndMutate(tested, selectedMates);
        }
        
        return new Subject(new float[] {0,0,0,0}, 0);
    }
}

class Subject {
    
    int score;
    float[] hypothesis;
    
    public Subject(float[] hypothesis, int score) {
        this.hypothesis = hypothesis; this.score = score;
    }
    
    public boolean isEqual(Subject other) {
        for (int i = 0; i < this.hypothesis.length; i++) {
            if (this.hypothesis[i] != other.hypothesis[i])
                return false;
        }
        return true;
    }
}

class EvoSpawn implements Callable<Subject> {
    
    public float[] hypothesis;
    private int numTests;
    
    public EvoSpawn(float[] hypothesis, int numTests) {
        this.hypothesis = hypothesis;
        this.numTests = numTests;
    }

    @Override
    public Subject call() throws Exception {
        AIChoice algo = new AIChoice(this.hypothesis);
        TileMerge game = new TileMerge(new Random(), algo);
        int score = game.numWins(numTests);
        return new Subject(this.hypothesis, score);
    }
}
