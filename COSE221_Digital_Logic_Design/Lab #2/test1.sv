/* *******************************
 *	COSE221 Lab #2
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

module test1(a, b, y, good);
	input			a, b, y;
	output			good;

	assign good = ((~a) & y) | ((~b) & y) | (a & b & (~y));
endmodule
