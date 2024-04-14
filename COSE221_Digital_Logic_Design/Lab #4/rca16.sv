/* *******************************
 *	COSE221 Lab #4
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

`timescale 1ns/1ps
// full adder (structural module)
module fadd (
	input	logic	a, b, cin,
	output	logic	s, cout
);

	logic 	[3:0]	n;	// internal nodes

	// sum: s = (a ^ b) ^ cin
	xor2 u_xor2_0(a, b, n[0]);
	xor2 u_xor2_1(n[0], cin, s);

	// cout: cout = (a*b) + (a*cin) + (b*cin)
	and2 u_and2_0(a, b, n[1]);
	and2 u_and2_1(a, cin, n[2]);
	and2 u_and2_2(b, cin, n[3]);
	or3 u_or3_0(n[1], n[2], n[3], cout);


endmodule

module rca16 (
	input					clk, rst_b,
	input	logic [15:0]	a, b, 
	input	logic			cin,
	output	logic [15:0]	s,
	output	logic			cout
);

	// registers for input signals
	logic	[15:0]	a_q, b_q;
	logic			cin_q;

	// t_pcq of D flip-flop is 0.010 ns
	always_ff @ (posedge clk or negedge rst_b) begin
		if (~rst_b) begin
			a_q <= 'b0;
			b_q <= 'b0;
			cin_q <= 'b0;
		end else begin
			a_q <= #(0.010) a;
			b_q <= #(0.010) b;
			cin_q <= #(0.010) cin;
		end
	end

	logic 	[15:0]	c;		// internal nodes (carry)
	logic	[15:0]	s_w;	// sum (output of an adder)

	fadd	u_fadd_0	(a_q[0], b_q[0], cin_q, s_w[0], c[0]);
	fadd	u_fadd_1	(a_q[1], b_q[1], c[0], s_w[1], c[1]);
	fadd	u_fadd_2	(a_q[2], b_q[2], c[1], s_w[2], c[2]);
	fadd	u_fadd_3	(a_q[3], b_q[3], c[2], s_w[3], c[3]);
	fadd	u_fadd_4	(a_q[4], b_q[4], c[3], s_w[4], c[4]);
	fadd	u_fadd_5	(a_q[5], b_q[5], c[4], s_w[5], c[5]);
	fadd	u_fadd_6	(a_q[6], b_q[6], c[5], s_w[6], c[6]);
	fadd	u_fadd_7	(a_q[7], b_q[7], c[6], s_w[7], c[7]);
	fadd	u_fadd_8	(a_q[8], b_q[8], c[7], s_w[8], c[8]);
	fadd	u_fadd_9	(a_q[9], b_q[9], c[8], s_w[9], c[9]);
	fadd	u_fadd_10	(a_q[10], b_q[10], c[9], s_w[10], c[10]);
	fadd	u_fadd_11	(a_q[11], b_q[11], c[10], s_w[11], c[11]);
	fadd	u_fadd_12	(a_q[12], b_q[12], c[11], s_w[12], c[12]);
	fadd	u_fadd_13	(a_q[13], b_q[13], c[12], s_w[13], c[13]);
	fadd	u_fadd_14	(a_q[14], b_q[14], c[13], s_w[14], c[14]);
	fadd	u_fadd_15	(a_q[15], b_q[15], c[14], s_w[15], c[15]);


	// registers for output signals
	always_ff @ (posedge clk or negedge rst_b) begin
		if (~rst_b) begin
			s <= 'b0;
			cout <= 'b0;
		end else begin
			s <= #(0.010) s_w;
			cout <= #(0.010) c[15];
		end
	end

endmodule
