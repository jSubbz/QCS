package qcs.model;

/**
 * Represents a single gate operation in the quantum circuit.
 * Compatible with Qiskit's OpenQASM syntax.
 */
public class GateOperation {

    /**
     * The name of the gate (e.g., "h", "cx", "rz").
     * Stored in lowercase for QASM compatibility.
     */
    public final String gateName;
    /**
     * An array of integers representing the indices of the qubits
     * on which this gate operation acts.
     */
    public final int[] qubits;

    /**
     * Constructs a new GateOperation.
     *
     * @param gateName The name of the gate. It will be converted to lowercase.
     * @param qubits   A variable number of integer arguments representing the qubit indices.
     */
    public GateOperation(String gateName, int... qubits) {
        this.gateName = gateName.toLowerCase();  // Normalize for QASM compatibility
        this.qubits = qubits;
    }

    @Override
    /**
     * Returns a string representation of the gate operation in OpenQASM format.
     * Example: "h q[0];" or "cx q[0],q[1];"
     * @return The OpenQASM string for this gate operation.
     */
    public String toString() {
        return gateName + " " + formatQubits() + ";";
    }

    /**
     * Formats the qubit indices into a comma-separated string suitable for OpenQASM.
     * Example: "q[0]" or "q[0],q[1]"
     * @return A string representing the formatted qubits.
     */
    private String formatQubits() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < qubits.length; i++) {
            sb.append("q[").append(qubits[i]).append("]");
            if (i < qubits.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}
