/* *******************************
 *	COSE221 Lab #2
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

module tristate(a, en, y);
	input			a, en;
	output	tri		y;

	assign y = (en) ? a: 1'bZ;
endmodule

module mux2(a, b, sel, y);
	input			a, b, sel;
	output	tri		y;

	/* FILL THIS */
	tristate u_tristate_0 (a, ~sel, y);
	tristate u_tristate_1 (b, sel, y);

endmodule

module test4(a, b, y, good);
	input			a, b, y;
	output	tri		good;

	/* INTERNAL NODES (TO BE DECLARED AS 'tri' TYPES) */
	tri		c, d, e;
	/* FILL THIS (STRUCTURAL MODEL) */
	mux2 u_mux2_c (1, 0, y, c);
	mux2 u_mux2_d (y, y, b, d);
	mux2 u_mux2_e (y, c, b, e);
	mux2 u_mux2_g (d, e, a, good);

endmodule
