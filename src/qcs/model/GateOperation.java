package qcs.model;

/**
 * Represents a single gate operation in the quantum circuit.
 * Compatible with Qiskit's OpenQASM syntax.
 */
public class GateOperation {
    public final String gateName;
    public final int[] qubits;

    public GateOperation(String gateName, int... qubits) {
        this.gateName = gateName.toLowerCase();  // Normalize for QASM compatibility
        this.qubits = qubits;
    }

    @Override
    public String toString() {
        return gateName + " " + formatQubits() + ";";
    }

    private String formatQubits() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < qubits.length; i++) {
            sb.append("q[").append(qubits[i]).append("]");
            if (i < qubits.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}
