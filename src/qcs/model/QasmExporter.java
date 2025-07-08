package qcs.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * QasmExporter writes a list of GateOperations to a Qiskit/OpenQASM file with step metadata.
 */
public class QasmExporter {

    /**
     * Writes gate operations to QASM file, grouping by step.
     *
     * @param file output .qasm file
     * @param qubitCount number of qubits
     * @param steps 2D list of gate operations per step
     */
    public static void writeQasm(File file, int qubitCount, List<List<GateOperation>> steps) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("OPENQASM 2.0;\n");
            writer.write("include \"qelib1.inc\";\n");
            writer.write(String.format("qreg q[%d];\n", qubitCount));

            for (int i = 0; i < steps.size(); i++) {
                writer.write("// STEP " + i + "\n");
                for (GateOperation op : steps.get(i)) {
                    writer.write(op.toString() + "\n");
                }
            }
        }
    }
}
