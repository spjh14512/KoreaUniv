/* *******************************
 *	COSE221 Lab #4
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */


`timescale 1ns/1ps
module fadd_cla (
	input	logic	a, b, cin,
	output	logic	p, g, s
);
	logic			n;	// internal node
	
	// propagate: p = a+b
	or2 	u_or2_0		(a, b, p);

	// generate: g = a*b
	and2 	u_and2_0	(a, b, g);

	// sum: sum = (a ^ b) ^ cin
	xor2	u_xor2_0	(a, b, n);
	xor2	u_xor2_1	(n, cin, s);


endmodule

// carry lookahead logic (4-bit)
module cll4 (
	input	logic	[3:0]	p, g,
	input	logic			cin,
	output	logic	[3:0]	cout
);

	logic	[11:0]	n;	// internal nodes
	
	// cout[0] = g[0] + p[0]*cin
	and2	u_and2_0	(p[0], cin, n[0]);
	or2		u_or2_0		(g[0], n[0], cout[0]);

	// cout[1] = g[1] + p[1]*g[0] + p[1]*p[0]*cin
	and2 	u_and2_1	(p[1], g[0], n[1]);
	and3	u_and3_0	(p[1], p[0], cin, n[2]);
	or3		u_or3_1		(g[1], n[1], n[2], cout[1]);
	
	// cout[2] = g[2] + p[2]*g[1] + p[2]*p[1]*g[0] + p[2]*p[1]*p[0]*cin
	and2	u_and2_2	(p[2], g[1], n[3]);
	and3	u_and3_1	(p[2], p[1], g[0], n[4]);
	and4	u_and4_0	(p[2], p[1], p[0], cin, n[5]);
	or4		u_or4_0		(g[2], n[3], n[4], n[5], cout[2]);

	// cout[3] = g[3:0] + p[3:0]*cin, g[3:0] = g[3] + p[3]g[2] + p[3]p[2]g[1] + p[3]p[2]p[1]g[0], p[3:0] = p[3]p[2]p[1]p[0]
	and2	u_and2_3	(p[3], g[2], n[6]);
	and3	u_and3_2	(p[3], p[2], g[1], n[7]);
	and4	u_and4_1	(p[3], p[2], p[1], g[0], n[8]);
	or4		u_or4_1		(g[3], n[6], n[7], n[8], n[9]);	//g[3:0]
	and4	u_and4_2	(p[3], p[2], p[1], p[0], n[10]);	// p[3:0]
	
	and2	u_and2_4	(n[10], cin, n[11]);
	or2		u_or2_1		(n[9], n[11], cout[3]);

endmodule

// 4-bit carry lookahead adder
module cla4 (
	input	logic [3:0]		a, b, 
	input	logic			cin,
	output	logic [3:0]		s,
	output	logic			cout
);

	logic	[3:0]	p, g;
	logic	[3:0]	n;	// internal nodes
	logic	[3:0]	c;	// carry
	
	fadd_cla	u_fadd_cla_0	(a[0], b[0], cin, p[0], g[0], s[0]);
	fadd_cla	u_fadd_cla_1	(a[1], b[1], c[0], p[1], g[1], s[1]);
	fadd_cla	u_fadd_cla_2	(a[2], b[2], c[1], p[2], g[2], s[2]);
	fadd_cla	u_fadd_cla_3	(a[3], b[3], c[2], p[3], g[3], s[3]);

	cll4		u_cll4_0		(p, g, cin, c);

	assign cout = c[3];

endmodule

// 16-bit carry lookahead adder
module cla16 (
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

	logic 	[3:0]	c;		// internal nodes, carry-outs from cll4
	logic	[15:0]	s_w;	// sum of an adder
	
	cla4	u_cla4_0	(a_q[3:0], b_q[3:0], cin_q, s_w[3:0], c[0]);
	cla4	u_cla4_1	(a_q[7:4], b_q[7:4], c[0], s_w[7:4], c[1]);
	cla4	u_cla4_2	(a_q[11:8], b_q[11:8], c[1], s_w[11:8], c[2]);
	cla4	u_cla4_3	(a_q[15:12], b_q[15:12], c[2], s_w[15:12], c[3]);

	// registers for output signals
	always_ff @ (posedge clk or negedge rst_b) begin
		if (~rst_b) begin
			s <= 'b0;
			cout <= 'b0;
		end else begin
			s <= #(0.010) s_w;
			cout <= #(0.010) c[3];
		end
	end

endmodule
