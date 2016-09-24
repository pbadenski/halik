/*
 *  Source code obtained from stack overflow:
 *  http://stackoverflow.com/questions/4719633/java-simple-neural-network-setup
 *
 *  Submitted by http://stackoverflow.com/users/544109/jerluc (Jeremy Lucas)
 *
 *  Licensed under cc by-sa 3.0 (http://creativecommons.org/licenses/by-sa/3.0/) with attribution required
 *
 */
package io.halik.xor_simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Neuron {
    private List<Neuron> inputs;
    private float weight;
    private float threshold;
    private boolean fired;

    public Neuron (float t) {
        threshold = t;
        fired = false;
        inputs = new ArrayList<Neuron>();
    }

    public void connect (Neuron ... ns) {
        Collections.addAll(inputs, ns);
    }

    public void setWeight (float newWeight) {
        weight = newWeight;
    }

    public void setWeight (boolean newWeight) {
        weight = newWeight ? 1.0f : 0.0f;
    }

    public float getWeight () {
        return weight;
    }

    public float fire () {
        if (inputs.size() > 0) {
            float totalWeight = 0.0f;
            for (Neuron n : inputs) {
                n.fire();
                totalWeight += (n.isFired()) ? n.getWeight() : 0.0f;
            }
            fired = totalWeight > threshold;
            return totalWeight;
        }
        else if (weight != 0.0f) {
            fired = weight > threshold;
            return weight;
        }
        else {
            return 0.0f;
        }
    }

    public boolean isFired () {
        return fired;
    }
}
