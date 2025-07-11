OPENQASM 2.0;
include "qelib1.inc";
qreg q[3];
// STEP 0
h q[0];
// STEP 1
cx q[0],q[1];
// STEP 2
x q[2];
