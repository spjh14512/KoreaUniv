<?
include "header.php";
include "config.php";    //데이터베이스 연결 설정파일
include "util.php";  
?>
<div class="container">
    <?
    $conn = dbconnect($host, $dbid, $dbpass, $dbname);
    $query = "select * from product natural join manufacturer";
    $result = mysqli_query($conn, $query);
    if (!$result) {
        die('Query Error : ' . mysqli_error());
    }
    ?>
    <form name='buy' action='buy.php' method='POST'>
        <p align='right'> 사용자 ID 입력: <input type='text' name='customer_id'></p>
        <table class="table table-striped table-bordered">
            <tr>
                <th>No.</th> 
                <th>제조사</th>
                <th>상품명</th>
                <th>가격</th>
                <th>등록일</th>
                <th>수량</th>
                <th>선택</th>
            </tr>
            <?
            $row_index = 1;
            while ($row = mysqli_fetch_array($result)) {
                echo "<tr>";
                echo "<td>{$row_index}</td>";
                echo "<td>{$row['manufacturer_name']}</td>";
                echo "<td><a href='product_view.php?product_id={$row['product_id']}'>{$row['product_name']}</a></td>";
                echo "<td>{$row['price']}</td>";
                echo "<td>{$row['added_datetime']}</td>";
                echo "<td width='10%'>
                	<input type='text' name='quantity_{$row['product_id']}' size='5'/>개</td>";
                echo "<td width='17%'>
                    <input type='checkbox' name=product_id[] value='{$row['product_id']}'>
                    </td>";
                echo "</tr>";
                $row_index++;
            }
            ?>
            
        </table>
        <div align='center'>
            <input type='submit' class='button primary small' value=구입>
        </div>
    </form>
</div>
<? include("footer.php") ?>