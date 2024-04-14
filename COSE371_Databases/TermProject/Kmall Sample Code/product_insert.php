<?
include "config.php";    //데이터베이스 연결 설정파일
include "util.php";      //유틸 함수

$conn = dbconnect($host,$dbid,$dbpass,$dbname);

$product_name = $_POST['product_name'];
$product_desc = $_POST['product_desc'];
$manufacturer_id = $_POST['manufacturer_id'];
$price = $_POST['price'];

$result = mysqli_query($conn, "insert into product (product_name, product_desc, manufacturer_id, price, added_datetime) values('$product_name', '$product_desc', '$manufacturer_id', '$price', NOW())");
if(!$result)
{
    msg('Query Error : '.mysqli_error($conn));
}
else
{
    s_msg ('성공적으로 입력 되었습니다');
    echo "<script>location.replace('product_list.php');</script>";
}

?>

