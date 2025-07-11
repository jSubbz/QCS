package qcs.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * A basic quantum simulator for a small number of qubits.
 * Supports single and two-qubit gates like X, H, CX, CCX.
 */
public class QuantumSimulator {
    private final int numQubits;
    private Complex[] state;

    public QuantumSimulator(int numQubits) {
        this.numQubits = numQubits;
        int size = 1 << numQubits;  // 2^n basis states
        state = new Complex[size];
        state[0] = new Complex(1.0, 0.0);  // |00...0⟩
        for (int i = 1; i < size; i++) {
            state[i] = new Complex(0.0, 0.0);
        }
    }

    /**
     * Applies a quantum gate operation to the current state.
     * @param op the gate operation
     */
    public void applyGate(GateOperation op) {
        switch (op.gateName) {
            case "x" -> {
                if (op.qubits.length >= 1) applyX(op.qubits[0]);
            }
            case "h" -> {
                if (op.qubits.length >= 1) applyH(op.qubits[0]);
            }
            case "z" -> {
                if (op.qubits.length >= 1) applyZ(op.qubits[0]);
            }
            case "s" -> {
                if (op.qubits.length >= 1) applyPhase(op.qubits[0], Math.PI / 2);
            }
            case "t" -> {
                if (op.qubits.length >= 1) applyPhase(op.qubits[0], Math.PI / 4);
            }
            case "cx", "cu" -> {
                if (op.qubits.length >= 2) applyCX(op.qubits[0], op.qubits[1]);
            }
            case "ccx" -> {
                if (op.qubits.length >= 3) applyCCX(op.qubits[0], op.qubits[1], op.qubits[2]);
            }
            case "swap" -> {
                if (op.qubits.length >= 2) applySWAP(op.qubits[0], op.qubits[1]);
            }
            default -> {
                System.out.println("Unsupported or malformed gate: " + op.gateName + " with " + op.qubits.length + " qubit(s)");
            }
        }
    }

    /**
     * Applies the Pauli-X gate to a single qubit.
     */
    private void applyX(int qubit) {
        int size = state.length;
        Complex[] newState = new Complex[size];
        for (int i = 0; i < size; i++) {
            int flipped = i ^ (1 << qubit);
            newState[i] = state[flipped].copy();
        }
        state = newState;
    }

    /**
     * Applies the Hadamard gate to a single qubit.
     */
    private void applyH(int qubit) {
        int size = state.length;
        Complex[] newState = new Complex[size];
        double norm = 1.0 / Math.sqrt(2);

        for (int i = 0; i < size; i++) {
            int bit = (i >> qubit) & 1;
            int flipped = i ^ (1 << qubit);

            Complex plus = state[i].scale(norm);
            Complex minus = state[flipped].scale(norm);

            newState[i] = bit == 0
                    ? plus.add(minus)
                    : plus.subtract(minus);
        }

        state = newState;
    }

    /**
     * Applies a CNOT (CX) gate with control and target.
     */
    private void applyCX(int control, int target) {
        int size = state.length;
        Complex[] newState = Arrays.stream(state).map(Complex::copy).toArray(Complex[]::new);

        for (int i = 0; i < size; i++) {
            if (((i >> control) & 1) == 1) {
                int flipped = i ^ (1 << target);
                Complex temp = newState[i];
                newState[i] = newState[flipped];
                newState[flipped] = temp;
            }
        }

        state = newState;
    }

    /**
     * Applies a Toffoli (CCX) gate: 2 controls, 1 target.
     */
    private void applyCCX(int c1, int c2, int target) {
        int size = state.length;
        Complex[] newState = Arrays.stream(state).map(Complex::copy).toArray(Complex[]::new);

        for (int i = 0; i < size; i++) {
            if (((i >> c1) & 1) == 1 && ((i >> c2) & 1) == 1) {
                int flipped = i ^ (1 << target);
                Complex temp = newState[i];
                newState[i] = newState[flipped];
                newState[flipped] = temp;
            }
        }

        state = newState;
    }

    /**
     * Returns a deep copy of the current state vector for rendering or stepping.
     */
    public List<Complex> copyState() {
        List<Complex> copy = new ArrayList<>(state.length);
        for (Complex c : state) {
            copy.add(c.copy());
        }
        return copy;
    }

    /**
     * Applies the Pauli-Z gate to a single qubit.
     * @param qubit The index of the qubit to apply the Z gate to.
     */
    private void applyZ(int qubit) {
        for (int i = 0; i < state.length; i++) {
            if (((i >> qubit) & 1) == 1) {
                state[i] = state[i].scale(-1); // Z gate flips sign of |1⟩
            }
        }
    }

    /**
     * Applies a phase shift gate to a single qubit.
     * @param qubit The index of the qubit to apply the phase shift to.
     * @param angle The angle of the phase shift in radians.
     */
    private void applyPhase(int qubit, double angle) {
        Complex phase = new Complex(Math.cos(angle), Math.sin(angle));
        for (int i = 0; i < state.length; i++) {
            if (((i >> qubit) & 1) == 1) {
                state[i] = state[i].multiply(phase);
            }
        }
    }

    /**
     * Applies a SWAP gate between two qubits.
     * @param q1 The index of the first qubit.
     * @param q2 The index of the second qubit.
     */
    private void applySWAP(int q1, int q2) {
        if (q1 == q2) return;

        for (int i = 0; i < state.length; i++) {
            int bit1 = (i >> q1) & 1;
            int bit2 = (i >> q2) & 1;
            if (bit1 != bit2) {
                int swappedIndex = i ^ (1 << q1) ^ (1 << q2);
                if (i < swappedIndex) {
                    Complex temp = state[i];
                    state[i] = state[swappedIndex];
                    state[swappedIndex] = temp;
                }
            }
        }
    }
}
