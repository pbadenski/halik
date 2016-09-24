/*
 *  Source code obtained from stack overflow:
 *  http://stackoverflow.com/questions/4719633/java-simple-neural-network-setup
 *
 *  Submitted by http://stackoverflow.com/users/544109/jerluc (Jeremy Lucas)
 *
 *  Licensed under cc by-sa 3.0 (http://creativecommons.org/licenses/by-sa/3.0/)
 *  with attribution required
 *
 */
package io.halik.xor_simulation;

public class XOR_NeuralNetwork_Example {
    public static void main (String [] args) {
        Neuron xor = new Neuron(0.5f);
        Neuron left = new Neuron(1.5f);
        Neuron right = new Neuron(0.5f);
        left.setWeight(-1.0f);
        right.setWeight(1.0f);
        xor.connect(left, right);

        for (String val : args) {
            Neuron op = new Neuron(0.0f);
            op.setWeight(Boolean.parseBoolean(val));
            left.connect(op);
            right.connect(op);
        }

        xor.fire();

        System.out.println("Result: " + xor.isFired());

    }
}