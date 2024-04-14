<?
include "header.php";
include "config.php";    //데이터베이스 연결 설정파일
include "util.php";      //유틸 함수

?>
<div class="container">
    <table class="table table-striped table-bordered">
        <tr>
            <th>구매번호</th>
            <th>구입자명</th>
            <th>구입일자</th>
            <th>총액</th>
        </tr>
    <?
    $conn = dbconnect($host, $dbid, $dbpass, $dbname);
    $query = "select buy_id, name, date_time, total_amount from buy natural join customer order by date_time DESC";
    $result = mysqli_query($conn, $query);
    while($row=mysqli_fetch_array($result)){
        echo "<tr><td><a href='buy_detail.php?buy_id={$row[0]}'>$row[0]</td>";
        echo "<td>$row[1]</td>";
        echo "<td>$row[2]</td>";
        echo "<td>$row[3]</td></tr>";
    }
    ?>
    </table>
</div>
    
<?
include "footer.php"
?>
