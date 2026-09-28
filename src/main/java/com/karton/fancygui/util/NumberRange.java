package com.karton.fancygui.util;

public record NumberRange (int from, int to) {
    public int[] getRangeArray() {
        int min = Math.min(from, to);
        int max = Math.max(from, to);

        int[] rangeArray = new int[max - min + 1];

        for (int i = 0; i < rangeArray.length; i++) {
            rangeArray[i] = min + i;
        }

        return rangeArray;
    }

    public boolean intersects(NumberRange other) {
        return this.from <= other.to()
                && other.from() <= this.to;
    }
}
