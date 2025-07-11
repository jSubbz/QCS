OPENQASM 2.0;
include "qelib1.inc";
qreg q[3];
// STEP 0
i q[0];
u q[0];
cu q[1],q[2];
// STEP 1
u q[0];
u q[0];
u q[1];
u q[2];
// STEP 2
cu q[1],q[2];
cu q[0],q[1];
// STEP 3
cu q[0],q[1];
cu q[1],q[2];
// STEP 4
cu q[1],q[2];
cu q[0],q[1];
