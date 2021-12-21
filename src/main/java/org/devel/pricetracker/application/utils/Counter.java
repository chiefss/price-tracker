package org.devel.pricetracker.application.utils;

public class Counter {

    private int count;

    public Counter() {
        this.count = 0;
    }

    public void increase() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
