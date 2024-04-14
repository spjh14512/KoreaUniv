/* *******************************
 *	COSE221 Lab #4
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

// This file includes basic gates required for design strcutural modesl
// You should use only the following modules for your design
// Available gates: xor2, and2, or2, and3, or3, and4, or4

`timescale 1ns/1ps
module xor2 (
	input	logic	a, b,
	output	logic	y
);
	assign #(0.010) y = a ^ b;	// delay: 10 ps
endmodule

module and2 (
	input	logic	a, b,
	output	logic	y
);
	assign #(0.015) y = a & b;	// delay: 15 ps
endmodule

module or2 (
	input	logic	a, b,
	output	logic	y
);
	assign #(0.015) y = a | b;	// delay: 15 ps
endmodule

module and3 (
	input	logic	a, b, c,
	output	logic	y
);
	assign #(0.016) y = a & b & c;	// delay: 16 ps
endmodule

module or3 (
	input	logic	a, b, c,
	output	logic	y
);
	assign #(0.016) y = a | b | c;	// delay: 16 ps
endmodule

module and4 (
	input	logic	a, b, c, d,
	output	logic	y
);
	assign #(0.017) y = a & b & c & d;	// delay: 17 ps
endmodule

module or4 (
	input	logic	a, b, c, d,
	output	logic	y
);
	assign #(0.017) y = a | b | c | d;	// delay: 17 ps
endmodule
