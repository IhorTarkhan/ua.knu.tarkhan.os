package org.example.os.lab2;

public class Common {


    static public double R1() {
        java.util.Random generator = new java.util.Random(System.currentTimeMillis());
        double U = generator.nextDouble();
        double V = generator.nextDouble();
//        double U = doubles.poll();
//        double V = doubles.poll();
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

