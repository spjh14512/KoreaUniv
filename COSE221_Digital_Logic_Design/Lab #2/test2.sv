/* *******************************
 *	COSE221 Lab #2
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

module test2(a, b, y, good);
	input			a, b, y;
	output			good;

	/* INTERNAL NODES */
	wire c, d, e;
	/* FILL THIS */
	assign c = y ? 0 : 1;
	assign d = y;
	assign e = b ? c : y;
	assign good = a ? e : d;

endmodule
