package org.example.os.lab2;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Common {
    private static final Queue<Double> doubles = new LinkedList<>(List.of(
            0.6738822030212246,
            0.9038304580623565,
            0.14593099990774294,
            0.5620797486903003,
            0.6432738245122781,
            0.11506421648393828
    ));


    static public double R1() {
//        java.util.Random generator = new java.util.Random(System.currentTimeMillis());
//        double U = generator.nextDouble();
//        double V = generator.nextDouble();
        double U = doubles.poll();
        double V = doubles.poll();
        double X = Math.sqrt(8 / Math.E) * (V - 0.5) / U;
        if (X * X > 5 - 4 * Math.exp(.25) * U) {
            return -1;
        }
        if (X * X >= 4 * Math.exp(-1.35) / U + 1.4) {
            return -1;
        }
        if (X * X >= -4 * Math.log(U)) {
            return -1;
        }
        return X;
    }

}

