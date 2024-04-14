/* *******************************
 *	COSE221 Lab #2
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

module test3(a, b, y, good);
	input			a, b, y;
	output			good;

	logic			good;

	/* FILL THIS */
	always_comb begin
		case ({a, b})
			'b00 : good = y;
			'b01 : good = y;
			'b10 : good = y;
			'b11 : good = ~y;
		endcase
	end

endmodule
