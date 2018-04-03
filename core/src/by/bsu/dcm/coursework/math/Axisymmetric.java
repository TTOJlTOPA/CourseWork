package by.bsu.dcm.coursework.math;

import com.badlogic.gdx.math.Vector2;

public class Axisymmetric extends EquilibriumFluid {
    private static class Integrand implements Function {
        private double[] approx;
        private double[] nodes;

        @Override
        public double calc(int index) {
            return approx[index] * nodes[index];
        }

        @Override
        public int getLength() {
            return nodes.length;
        }

        @Override
        public void setParams(Object... params) {
            this.approx = (double[]) params[0];
            this.nodes = (double[]) params[1];
        }
    }

    public Axisymmetric() {
        this(0.0, 0.0, 0.0, 0);
    }

    public Axisymmetric(double alpha, double bond, double epsilon, int splitNum) {
        super(new Integrand(), alpha, bond, epsilon, splitNum);
    }

    private double[] calcCoefs(double[] prevApprox, double[] nodes, double step) {
        double[] coefs = new double[splitNum + 1];
        double halfStep = step / 2.0;

        coefs[0] = 0.0;

        for (int i = 1; i < coefs.length; i++) {
            coefs[i] = (nodes[i] - halfStep) / Math.sqrt(1.0 + Math.pow((prevApprox[i] - prevApprox[i - 1]) / step, 2.0));
        }

        return coefs;
    }

    private double[] calcNextApproximation(double[] prevApprox, double[] nodes, double step) {
        double[][] leftPart = new double[splitNum + 1][splitNum + 1];
        double[] rightPart = new double[splitNum + 1];
        double[] coefs = calcCoefs(prevApprox, nodes, step);
        double integral;
        double q;

        integrand.setParams(prevApprox, nodes);

        integral = 2.0 * Math.PI * Util.calcIntegralTrapeze(integrand, step);
        q = -2.0 * Math.sin(alpha) - bond * Math.pow(integral, 1.0 / 3.0) / Math.PI;

        leftPart[0][0] = -(1.0 / step + (step / 4.0) * (bond / Math.pow(integral, 2.0 / 3.0)));
        leftPart[0][1] = 1.0 / step;
        leftPart[splitNum][splitNum] = 1.0;

        rightPart[0] = step * q / 4.0;
        rightPart[splitNum] = 0.0;

        for (int i = 1; i < splitNum; i++) {
            leftPart[i][i - 1] = coefs[i];
            leftPart[i][i] = -(coefs[i] + coefs[i + 1]);
            leftPart[i][i + 1] = coefs[i + 1];

            rightPart[i] = nodes[i] * step * step * (bond * prevApprox[i] / Math.pow(integral, 2.0 / 3.0) + q);
        }

        return Util.calcRightSweep(leftPart, rightPart);
    }

    private double[] calcInitialApproximation(double[] nodes) {
        double[] init = new double[splitNum + 1];

        for (int i = 0; i < init.length; i++) {
            init[i] = Math.sqrt(1.0 - Math.pow(nodes[i], 2.0));
        }

        return init;
    }

    @Override
    public void calcResult() {
        Vector2[] points = new Vector2[splitNum + 1];
        double[] nodes = new double[splitNum + 1];
        double[] prevApprox;
        double[] nextApprox;
        double step = 1.0 / splitNum;
        int interations = 0;

        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = i * step;
        }

        nextApprox = calcInitialApproximation(nodes);

        do {
            prevApprox = nextApprox;
            nextApprox = calcNextApproximation(prevApprox, nodes, step);
            interations++;
        } while (Util.norm(nextApprox, prevApprox) > epsilon);

        for (int i = 0; i < nodes.length; i++) {
            points[i] = new Vector2((float) nodes[i], (float) nextApprox[i]);
        }
        graphPoints.points = points;

        System.out.println("Axisymmetric iterations num: " + interations);
    }
}
