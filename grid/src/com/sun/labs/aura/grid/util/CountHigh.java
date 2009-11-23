package com.sun.labs.aura.grid.util;

public class CountHigh {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int x = Integer.parseInt(args[0]);

        for(int j = 0; j < x; j++) {
            for(int i = 0; i < 10; i++) {
                double y = Math.pow(j, i);
                System.out.format("%d %d %.2f", j, i, y);
            }
        }
    }
}
