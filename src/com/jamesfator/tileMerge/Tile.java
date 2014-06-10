package com.jamesfator.tileMerge;

public class Tile {
    int x;
    int y;
    int value;
    
    public Tile(Tile other) {
        this.x = other.x;
        this.y = other.y;
        this.value = other.value;
    }
    
    public Tile(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }
    
    public String prettyString() {
        if (this.value == 0) {
            return "[    ]";
        }
        String valStr = String.valueOf(this.value);
        if (value < 10) {
            valStr = " " + valStr + "  ";
        } else if (value < 100) {
            valStr = " " + valStr + " ";
        } else if (value < 1000) {
            valStr = " " + valStr + "";
        }
        return "[" + valStr + "]";
    }
    
    public static String prettyString(int val) {
        if (val == 0) {
            return "[    ]";
        }
        String valStr = String.valueOf(val);
        if (val < 10) {
            valStr = " " + valStr + "  ";
        } else if (val < 100) {
            valStr = " " + valStr + " ";
        } else if (val < 1000) {
            valStr = " " + valStr + "";
        }
        return "[" + valStr + "]";
    }
    
    public String toString() {
        return String.valueOf(this.value);
    }
}
