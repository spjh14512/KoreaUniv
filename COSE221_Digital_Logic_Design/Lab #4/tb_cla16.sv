/* ************************
 *	COSE221 Lab #4
 *
 *	Author: Gunjae Koo (gunjaekoo2@korea.ac.kr)
 * ************************
 */

`timescale 1ns/1ps
`define CLK_T 0.145 // 0.145 미만으로는 INCORRECT를 받음
`define FF 0.001
module tb_cla16();

	integer f;
	logic 		clk, rst_b;

	initial clk = 1'b1;
	always #(`CLK_T/2.0) clk = ~clk;

	logic	[15:0]	a, b;
	logic	[15:0]	a_d1, b_d1;
	logic	[15:0]	a_d2, b_d2;
	logic			cin, cin_d1, cin_d2;
	logic	[15:0]	s, s_gold;
	logic			cout, cout_gold;

	initial begin
		$dumpfile("tb_cla16.vcd");
		$dumpvars(0, tb_cla16);
		f = $fopen("tb_cla16.out", "w");
		
		rst_b = 1'b0;
		a = 'b0; b = 'b0; cin = 'b0;
		a_d1 = 'b0; b_d1 = 'b0; cin_d1 = 'b0;
		a_d2 = 'b0; b_d2 = 'b0; cin_d2 = 'b0;
		repeat (2) @ (posedge clk);
		#(`FF) rst_b = 1'b1;
		repeat (2) @ (posedge clk);
		#(`FF) a = 16'h0000; b = 16'hFFFF; cin = 1'b0;
		@ (posedge clk);
		#(`FF) a = 16'h0000; b = 16'hFFFF; cin = 1'b1;

		repeat (1024) begin
			@ (posedge clk);
			#(`FF) a = a + 1;
		end
		repeat (1024) begin
			@ (posedge clk);
			#(`FF) b = b + 1;
		end
		
		repeat (2) @ (posedge clk);
		$fclose(f);
		$finish(1);
	end

	always @ (posedge clk) begin
		a_d1 <= #(`FF) a;
		b_d1 <= #(`FF) b;
		cin_d1 <= #(`FF) cin;
		
		a_d2 <= #(`FF) a_d1;
		b_d2 <= #(`FF) b_d1;
		cin_d2 <= #(`FF) cin_d1;
	end

	assign {cout_gold, s_gold} = {1'b0, a_d2} + b_d2 + cin_d2;

	always @ (negedge clk) begin
		if (s!==s_gold || cout!==cout_gold) begin
			$write("Golden: %04x + %04x + %1b = {%1b, %04x}\t", a_d2, b_d2, cin_d2, cout_gold, s_gold);
			$write("DUT: {%1b, %04x}\t", cout, s);
			$write("Incorrect!!");
			$write("\n");
		end
		
		$fwrite(f, "Golden: %04x + %04x + %1b = {%1b, %04x}\t", a_d2, b_d2, cin_d2, cout_gold, s_gold);
		$fwrite(f, "DUT: {%1b, %04x}\t", cout, s);
		if (s!==s_gold || cout!==cout_gold) $fwrite(f, "Incorrect!!");
		$fwrite(f, "\n");
	end

	cla16	dut	(clk, rst_b, a, b, cin, s, cout);

endmodule
