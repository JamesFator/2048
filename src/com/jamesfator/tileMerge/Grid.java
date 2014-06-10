package com.jamesfator.tileMerge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class Grid {
    
    static int nMoves = 4;
//    static int size = 3;
//    static int goal = 7; 
    static int size = 4;
    static int goal = 11; 
    boolean won;
    boolean playerTurn;
    
    int[][] cells;
    
    public Grid(Grid original) {
        this.won = false;
        this.playerTurn = original.playerTurn;
        this.cells = new int[size][];
        for (int x = 0; x < size; x++)
            this.cells[x] = Arrays.copyOf(original.cells[x],
                    original.cells[x].length);
    }
    
    public Grid() {
        this.won = false;
        this.playerTurn = true;
        this.cells = new int[size][size];
    }
    
    public void reset() {
        this.won = false;
        this.playerTurn = true;
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                setCell(x, y, 0);
    }
    
    public int cellAt(int x, int y) {
        return cells[x][y];
    }
    
    public void setCell(int x, int y, int value) {
        cells[x][y] = value;
        if (value >= goal)
            won = true;
    }
    
    public void setCell(Tile tile) {
        cells[tile.x][tile.y] = tile.value;
        if (tile.value >= goal)
            won = true;
    }
    
    public boolean isEmptyCell(int x, int y) {
        return cellAt(x, y) == 0;
    }
    
    public boolean isEqual(Grid other) {
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (cellAt(x, y) != other.cellAt(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * 
     * @param direction - 0:up, 1:right, 2:down, 3:left
     * @return score acquired, or -1 if grid did not change
     */
    public int shiftBoard(int direction) {
        boolean moved = false;
        int additionalScore = 0;
        switch (direction) {
        case 0: // up
            for (int y = 0; y < size; y++) {
                ArrayList<Integer> current = new ArrayList<Integer>();
                int lastMerged = -1;
                for (int x = 0; x < size; x++) {
                    if (!isEmptyCell(x, y)) {
                        int curIndex = current.size()-1;
                        if (curIndex >= 0 &&
                                current.get(curIndex) == cellAt(x, y) &&
                                lastMerged != curIndex) {
                            // Merge and prevent this tile from merging again
                            lastMerged = curIndex;
                            int score = cellAt(x, y)+1;
                            current.set(curIndex, score);
                            additionalScore += score;
                        } else {
                            // Add the next item
                            current.add(cellAt(x, y));
                        }
                    }
                }
                if (current.size() > 0) {
                    int x;
                    for (x = 0; x < current.size(); x++) {
                        // set the cells in the new order
                        if (cellAt(x,y) != current.get(x)) moved = true;
                        setCell(x, y, current.get(x));
                    }
                    // Fill in the remaining zeros
                    for (; x < size; x++) setCell(x, y, 0);
                }
            }
            break;
        case 1: // right
            for (int x = 0; x < size; x++) {
                ArrayList<Integer> current = new ArrayList<Integer>();
                int lastMerged = -1;
                for (int y = size-1; y > -1; y--) {
                    if (!isEmptyCell(x, y)) {
                        int curIndex = current.size()-1;
                        if (curIndex >= 0 &&
                                current.get(curIndex) == cellAt(x, y) &&
                                lastMerged != curIndex) {
                            // Merge and prevent this tile from merging again
                            lastMerged = curIndex;
                            int score = cellAt(x, y)+1;
                            current.set(curIndex, score);
                            additionalScore += score;
                        } else {
                            // Add the next item
                            current.add(cellAt(x, y));
                        }
                    }
                }
                if (current.size() > 0) {
                    int y;
                    for (y = size-1; y > size-current.size()-1; y--) {
                        // set the cells in the new order
                        if (cellAt(x,y) != current.get(size-1-y)) moved = true;
                        setCell(x, y, current.get(size-1-y));
                    }
                    // Fill in the remaining zeros
                    for (; y > -1; y--) setCell(x, y, 0);
                }
            }
            break;
        case 2: // down
            for (int y = 0; y < size; y++) {
                ArrayList<Integer> current = new ArrayList<Integer>();
                int lastMerged = -1;
                for (int x = size-1; x > -1; x--) {
                    if (cellAt(x, y) != 0) {
                        int curIndex = current.size()-1;
                        if (curIndex >= 0 &&
                                current.get(curIndex) == cellAt(x, y) &&
                                lastMerged != curIndex) {
                            // Merge and prevent this tile from merging again
                            lastMerged = curIndex;
                            int score = cellAt(x, y)+1;
                            current.set(curIndex, score);
                            additionalScore += score;
                        } else {
                            // Add the next item
                            current.add(cellAt(x, y));
                        }
                    }
                }
                if (current.size() > 0) {
                    int x;
                    for (x = size-1; x > size-current.size()-1; x--) {
                        // set the cells in the new order
                        if (cellAt(x,y) != current.get(size-1-x)) moved = true;
                        setCell(x, y, current.get(size-1-x));
                    }
                    // Fill in the remaining zeros
                    for (; x > -1; x--) setCell(x, y, 0); 
                }
            }
            break;
        case 3: // left
            for (int x = 0; x < size; x++) {
                ArrayList<Integer> current = new ArrayList<Integer>();
                int lastMerged = -1;
                for (int y = 0; y < size; y++) {
                    if (!isEmptyCell(x, y)) {
                        int curIndex = current.size()-1;
                        if (curIndex >= 0 &&
                                current.get(curIndex) == cellAt(x, y) &&
                                lastMerged != curIndex) {
                            // Merge and prevent this tile from merging again
                            lastMerged = curIndex;
                            int score = cellAt(x, y)+1;
                            current.set(curIndex, score);
                            additionalScore += score;
                        } else {
                            // Add the next item
                            current.add(cellAt(x, y));
                        }
                    }
                }
                if (current.size() > 0) {
                    int y;
                    for (y = 0; y < current.size(); y++) {
                        // set the cells in the new order
                        if (cellAt(x,y) != current.get(y)) moved = true;
                        setCell(x, y, current.get(y));
                    }
                    // Fill in the remaining zeros
                    for (; y < size; y++) setCell(x, y, 0);
                }
            }
            break;
        }
        if (!moved) {
            additionalScore = -1;
        } else {
            this.playerTurn = false;
        }
        return additionalScore;
    }
    
    public boolean isMoveAvailable() {
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++) 
                if (isEmptyCell(x,y)) return true;
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size-1; y += 2) {
                // vertical
                if (cellAt(x,y) == cellAt(x,y+1)) {
                    return true;
                }
                if (y < size-2 && cellAt(x,y+1) == cellAt(x,y+2)) {
                    return true;
                }
                // horizontal
                if (x < size-1 && (cellAt(x,y) == cellAt(x+1,y) ||
                        cellAt(x,y+1) == cellAt(x+1,y+1))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public ArrayList<Tile> availableCells() {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (isEmptyCell(x, y))
                    tiles.add(new Tile(x, y, 0));
            }
        }
        return tiles;
    }
    
    public boolean isGameTerminated() {
        return !isMoveAvailable() || this.won;
    }
    
    public int highestValue() {
        int highestValue = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int val = cellAt(x,y);
                if (val > highestValue)
                    highestValue = val;
            }
        }
        return highestValue;
    }
    
    public int smoothness() {
        int smoothness = 0;
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (!isEmptyCell(x, y)) {
                    int val = cellAt(x, y);
                    // Check right
                    for (int next = x+1; next < size; next++) {
                        if (!isEmptyCell(next, y)) {
                            int targetVal = cellAt(next, y);
                            smoothness -= Math.abs(val - targetVal);
                            break;
                        }
                    }
                    // Check down
                    for (int next = y+1; next < size; next++) {
                        if (!isEmptyCell(x, next)) {
                            int targetVal = cellAt(x, next);
                            smoothness -= Math.abs(val - targetVal);
                            break;
                        }
                    }
                }
            }
        }
        return smoothness;
    }
    
    public int monotonicity() {
        float[] totals = new float[] {0,0,0,0};
        
        // up/down
        for (int x = 0; x < size; x++) {
            int current = 0;
            int next = current + 1;
            while (next < size) {
                while (next < size && isEmptyCell(x, next))
                    next++;
                if (next >= size) next--;
                double curVal = cellAt(x, current);
                double nextVal = cellAt(x, next);
                if (curVal > nextVal) {
                    totals[0] += nextVal - curVal;
                } else if (nextVal > curVal) {
                    totals[1] += curVal - nextVal;
                }
                current = next;
                next++;
            }
        }
        
        // left/right
        for (int y = 0; y < size; y++) {
            int current = 0;
            int next = current + 1;
            while (next < size) {
                while (next < size && isEmptyCell(next, y))
                    next++;
                if (next >= size) next--;
                double curVal = cellAt(current, y);
                double nextVal = cellAt(next, y);
                if (curVal > nextVal) {
                    totals[2] += nextVal - curVal;
                } else if (nextVal > curVal) {
                    totals[3] += curVal - nextVal;
                }
                current = next;
                next++;
            }
        }
        
        return (int)(Math.max(totals[0], totals[1]) +
                Math.max(totals[2], totals[3]));
    }
    
    public int numEmptyCells() {
        int count = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (isEmptyCell(x, y))
                    count++;
            }
        }
        return count;
    }
    
    public int islands() {
        int islands = 0;
        int[][] visited = new int[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (visited[x][y] == 0 && !isEmptyCell(x,y)) {
                    // if the cell has value and has not been visited yet
                    islands++;
                    visited[x][y] = 1;
                    LinkedList<Tile> neighbors = new LinkedList<Tile>();
                    if (x < size-1 && visited[x+1][y] == 0)
                        neighbors.push(new Tile(x+1, y, 0));
                    if (y < size-1 && visited[x][y+1] == 0)
                        neighbors.push(new Tile(x, y+1, 0));
                    if (x > 0 && visited[x-1][y] == 0)
                        neighbors.push(new Tile(x-1, y, 0));
                    if (y > 0 && visited[x][y-1] == 0)
                        neighbors.push(new Tile(x, y-1, 0));
                    while (neighbors.isEmpty() == false) {
                        Tile curT = neighbors.pop();
                        int cX = curT.x;
                        int cY = curT.y;
                        // If tile is not empty and not visited
                        visited[cX][cY] = 1;
                        if (!isEmptyCell(cX,cY)) {
                            if (cX < size-1 && visited[cX+1][cY] == 0)
                                neighbors.push(new Tile(cX+1, cY, 0));
                            if (cY < size-1 && visited[cX][cY+1] == 0)
                                neighbors.push(new Tile(cX, cY+1, 0));
                            if (cX > 0 && visited[cX-1][cY] == 0)
                                neighbors.push(new Tile(cX-1, cY, 0));
                            if (cY > 0 && visited[cX][cY-1] == 0)
                                neighbors.push(new Tile(cX, cY-1, 0));
                        }
                    }
                }
            }
        }
        return islands;
    }
    
    /**
     * 
     * @return integer array containing each at index:
     *  0 => Maximum value in board
     *  1 => Number of empty cells
     *  2 => Smoothness of the grid
     *  3 => How monotonic the grid is
     */
    public float[] getFeatures() {
        float[] totals = new float[] {0,0,0,0};
        float[] features = new float[4];
        int eCount = 0;
        int highestValue = 0;
        int smoothness = 0;
        for (int x = 0; x < size; x++) {
            // Monoticity x
            int monoCur = 0;
            int monoNext = monoCur + 1;
            while (monoNext < size) {
                while (monoNext < size && isEmptyCell(x, monoNext))
                    monoNext++;
                if (monoNext >= size) monoNext--;
                double curVal = cellAt(x, monoCur);
                double nextVal = cellAt(x, monoNext);
                if (curVal > nextVal) {
                    totals[0] += nextVal - curVal;
                } else if (nextVal > curVal) {
                    totals[1] += curVal - nextVal;
                }
                monoCur = monoNext;
                monoNext++;
            }
            
            for (int y = 0; y < size; y++) {
                // Monoticity y 
                monoCur = 0;
                monoNext = monoCur + 1;
                while (monoNext < size) {
                    while (monoNext < size && isEmptyCell(monoNext, y))
                        monoNext++;
                    if (monoNext >= size) monoNext--;
                    double curVal = cellAt(monoCur, y);
                    double nextVal = cellAt(monoNext, y);
                    if (curVal > nextVal) {
                        totals[2] += nextVal - curVal;
                    } else if (nextVal > curVal) {
                        totals[3] += curVal - nextVal;
                    }
                    monoCur = monoNext;
                    monoNext++;
                }
                
                if (!isEmptyCell(x, y)) {
                    // Smoothness
                    int val = cellAt(x, y);
                    // Check right
                    for (int next = x+1; next < size; next++) {
                        if (!isEmptyCell(next, y)) {
                            int targetVal = cellAt(next, y);
                            smoothness -= Math.abs(val - targetVal);
                            break;
                        }
                    }
                    // Check down
                    for (int next = y+1; next < size; next++) {
                        if (!isEmptyCell(x, next)) {
                            int targetVal = cellAt(x, next);
                            smoothness -= Math.abs(val - targetVal);
                            break;
                        }
                    }
                }
                
                // highest val and empty count
                int val = cellAt(x,y);
                if (val > highestValue)
                    highestValue = val;
                else if (val == 0)
                    eCount++;
            }
        }

        features[0] = highestValue;
        features[1] = (eCount > 0 ? (float)(Math.log(eCount)) : 0);
        features[2] = smoothness;
        features[3] = (Math.max(totals[0], totals[1]) +
                Math.max(totals[2], totals[3]));
        return features;
    }
    
    public int betaEval() {
        int smoothness = 0;
        int islands = 0;
        int[][] visited = new int[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (!isEmptyCell(x, y)) {
                    // Calculate visited
                    if (visited[x][y] == 0) {
                        // if the cell has value and has not been visited yet
                        islands++;
                        visited[x][y] = 1;
                        LinkedList<Tile> neighbors = new LinkedList<Tile>();
                        if (x < size-1 && visited[x+1][y] == 0)
                            neighbors.push(new Tile(x+1, y, 0));
                        if (y < size-1 && visited[x][y+1] == 0)
                            neighbors.push(new Tile(x, y+1, 0));
                        if (x > 0 && visited[x-1][y] == 0)
                            neighbors.push(new Tile(x-1, y, 0));
                        if (y > 0 && visited[x][y-1] == 0)
                            neighbors.push(new Tile(x, y-1, 0));
                        while (neighbors.isEmpty() == false) {
                            Tile curT = neighbors.pop();
                            int cX = curT.x;
                            int cY = curT.y;
                            // If tile is not empty and not visited
                            visited[cX][cY] = 1;
                            if (!isEmptyCell(cX,cY)) {
                                if (cX < size-1 && visited[cX+1][cY] == 0)
                                    neighbors.push(new Tile(cX+1, cY, 0));
                                if (cY < size-1 && visited[cX][cY+1] == 0)
                                    neighbors.push(new Tile(cX, cY+1, 0));
                                if (cX > 0 && visited[cX-1][cY] == 0)
                                    neighbors.push(new Tile(cX-1, cY, 0));
                                if (cY > 0 && visited[cX][cY-1] == 0)
                                    neighbors.push(new Tile(cX, cY-1, 0));
                            }
                        }
                    }
                    
                    // Calculate smoothness
                    int val = cellAt(x, y);
                    // Check right
                    for (int next = x+1; next < size; next++) {
                        if (!isEmptyCell(next, y)) {
                            int targetVal = cellAt(next, y);
                            smoothness -= Math.abs(val - targetVal);
                            break;
                        }
                    }
                    // Check down
                    for (int next = y+1; next < size; next++) {
                        if (!isEmptyCell(x, next)) {
                            int targetVal = cellAt(x, next);
                            smoothness -= Math.abs(val - targetVal);
                            break;
                        }
                    }
                }
            }
        }
        return -smoothness + islands;
    }
    
    public void placeRandomCell(Random rand) {
        ArrayList<Tile> available = availableCells();
        if (available.size() == 0) return;
        Tile t = available.get(rand.nextInt(available.size()));
        // 10% chance of dropping a 4
        int val = rand.nextFloat() < 0.9 ? 1 : 2;
        setCell(t.x, t.y, val);
        if (!this.playerTurn) this.playerTurn = true;
    }
    
    public String prettyString() {
        String retStr = "";
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int val = cellAt(x,y);
                if (val != 0) val = (int)Math.pow(2, val);
                retStr += Tile.prettyString(val);
                retStr += " ";
            }
            retStr += "\n";
        }
        return retStr;
    }
    
    public String toString() {
        String retStr = "";
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int val = cellAt(x,y);
                if (val != 0) val = (int)Math.pow(2, val);
                retStr += val;
                retStr += " ";
            }
            retStr += "\n";
        }
        return retStr;
    }
}
