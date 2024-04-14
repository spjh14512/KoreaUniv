<?
include "config.php";    //데이터베이스 연결 설정파일
include "util.php";      //유틸 함수

$conn = dbconnect($host,$dbid,$dbpass,$dbname);

$product_id = $_GET['product_id'];

$pid_ret = mysqli_query($conn, "select product_id from buy_item where product_id = $product_id");

if(mysqli_fetch_array($result)){
	$ret = mysqli_query($conn, "delete from product where product_id = $product_id");

	if(!$ret)
	{
	    msg('Query Error : '.mysqli_error($conn));
	}
	else
	{
	    s_msg ('성공적으로 삭제 되었습니다');
	    echo "<meta http-equiv='refresh' content='0;url=product_list.php'>";
	}	
}
else{
	s_msg ('이미 주문된 제품이므로 삭제할 수 없습니다.');
    echo "<meta http-equiv='refresh' content='0;url=product_list.php'>";
}

?>

