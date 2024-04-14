/* ************************
 *	COSE221 Lab #2
 *
 *	Author: Gunjae Koo (gunjaekoo2@korea.ac.kr)
 * ************************
 */

`timescale 1ns/1ps
module tb_test1();

	logic a = 'b0;
	logic b = 'b0;
	logic y = 'b0;
	logic good;
	
	always #(5) a = ~a;
	always #(10) b = ~b;
	always #(20) y = ~y;

	integer f;
	logic clk = 1'b0;	// for sampling
	always #(2.5) clk = ~clk;

	always @ (posedge clk) begin
		$write("%1b, %1b, %1b; %1b @ %2.3f\n", a, b, y, good, $realtime);
		$fwrite(f, "%1b, %1b, %1b; %1b @ %2.3f\n", a, b, y, good, $realtime);
	end

	initial begin
		$dumpfile("tb_test1.vcd");
		$dumpvars(0, tb_test1);
		f = $fopen("tb_test1.out", "w");
		#(45)
		$fclose(f);
		$finish;
	end

	test1 dut (a, b, y, good);

endmodule
