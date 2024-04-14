/* ************************
 *	COSE221 Lab #3
 *
 *	Author: Gunjae Koo (gunjaekoo2@korea.ac.kr)
 * ************************
 */

`timescale 1ns/1ps
`define CLK_T 10
module tb_fsm1();

	integer f;
	logic 		clk, reset_b;

	initial clk = 1'b1;
	always #(`CLK_T/2) clk = ~clk;

	logic 		ta, tb;
	logic [1:0] la, lb;

	initial begin
		$dumpfile("tb_fsm1.vcd");
		$dumpvars(0, tb_fsm1);
		f = $fopen("tb_fsm1.out", "w");
		
		reset_b = 1'b0;
		ta = 1'b1; tb = 1'b0;
		repeat (2) @ (posedge clk);
		#(1) reset_b = 1'b1;
		repeat (2) @ (posedge clk);
		#(1) ta = 1'b1; tb = 1'b1;
		repeat (2) @ (posedge clk);
		#(1) ta = 1'b0; tb = 1'b1;
		repeat (4) @ (posedge clk);
		#(1) ta = 1'b1; tb = 1'b1;
		repeat (2) @ (posedge clk);
		#(1) ta = 1'b1; tb = 1'b0;
		repeat (4) @ (posedge clk);
		#(1) ta = 1'b0; tb = 1'b0;
		repeat (5) @ (posedge clk);

		$fclose(f);
		$finish(1);
	end


	always @ (negedge clk) begin
		$write("%02b, %02b @ %2.3f\n", la, lb, $realtime);
		$fwrite(f, "%02b, %02b @ %2.3f\n", la, lb, $realtime);
	end

	fsm1 dut (clk, reset_b, ta, tb, la, lb);

endmodule
