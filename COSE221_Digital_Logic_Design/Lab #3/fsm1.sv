/* *******************************
 *	COSE221 Lab #3
 *
 *	Author: Gunjae Koo (gunjaekoo@korea.ac.kr)
 *
 * *******************************
 */

module fsm1(
	input			clk,
	input			reset_n,	// asychronous negative reset
	input			ta, tb,
	output	[1:0]	la, lb
);

	logic	[1:0]	la, lb;

	localparam S0 = 2'b00,
		       S1 = 2'b01,
			   S2 = 2'b10,
			   S3 = 2'b11;

	localparam GREEN = 2'b00,
		       YELLOW = 2'b01,
			   RED = 2'b10;

	logic [1:0]	state_curr, state_next;

	// state register
	always_ff @ (posedge clk or negedge reset_n) begin
		if (~reset_n) 	state_curr <= S0;
		else			state_curr <= state_next;
	end

	// next state logic
	always_comb begin
		case (state_curr)
			S0 : state_next = ta ? S0 : S1;
			S1 : state_next = S2;
			S2 : state_next = tb ? S2 : S3;
			S3 : state_next = S0;
		endcase
	end

	// output logic
	always_comb begin
		case (state_curr)
			S0 : begin
				la = GREEN;
				lb = RED;
			end
			S1 : begin
				la = YELLOW;
				lb = RED;
			end
			S2 : begin
				la = RED;
				lb = GREEN;
			end
			S3 : begin
				la = RED;
				lb = YELLOW;
			end
		endcase
	end

	// synthesis translate_off
	logic [2*8-1:0] state_dbg;

	always_comb begin
		case (state_curr)
			S0: state_dbg = "S0";
			S1: state_dbg = "S1";
			S2: state_dbg = "S2";
			S3: state_dbg = "S3";
		endcase
	end
	// synthesis translate_on

endmodule
