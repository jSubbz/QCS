# simulate_steps.py
import sys
import json
from qiskit import QuantumCircuit
from qiskit.quantum_info import Statevector

def read_steps_from_qasm(file_path):
    with open(file_path, 'r') as f:
        lines = f.readlines()

    header = []
    steps = []
    current_step = []

    for line in lines:
        if line.startswith("OPENQASM") or "include" in line or "qreg" in line:
            header.append(line.strip())
        elif "// STEP" in line:
            if current_step:
                steps.append(current_step)
            current_step = [line.strip()]
        elif line.strip():
            current_step.append(line.strip())

    if current_step:
        steps.append(current_step)

    return header, steps

def simulate_steps(header, steps, num_qubits):
    state = Statevector.from_label("0" * num_qubits)
    states = []

    for step_num, step_lines in enumerate(steps):
        qc = QuantumCircuit(num_qubits)
        for line in step_lines:
            if not line.startswith("//"):
                exec_line = "\n".join(header + [f"qreg q[{num_qubits}];", line])
                partial = QuantumCircuit.from_qasm_str(exec_line)
                qc.compose(partial, inplace=True)
        state = state.evolve(qc)
        states.append(state.data.tolist())

    return states

def main(qasm_path, out_path="step_states.json"):
    header, steps = read_steps_from_qasm(qasm_path)
    qubit_count = 0
    for line in header:
        if "qreg" in line:
            qubit_count = int(line[line.index('[')+1 : line.index(']')])

    state_list = simulate_steps(header, steps, qubit_count)

    with open(out_path, 'w') as out:
        json.dump({
            "qubits": qubit_count,
            "steps": state_list
        }, out)

    print(f"Saved {len(state_list)} steps to {out_path}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python simulate_steps.py your.qasm")
    else:
        main(sys.argv[1])
