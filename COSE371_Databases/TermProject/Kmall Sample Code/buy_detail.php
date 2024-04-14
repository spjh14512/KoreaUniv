<?
include "header.php";
include "config.php";    //데이터베이스 연결 설정파일
include "util.php";      //유틸 함수


$conn = dbconnect($host, $dbid, $dbpass, $dbname);

if (array_key_exists("buy_id", $_GET)) {
    $buy_id = $_GET["buy_id"];
    $query = "select buy_id, customer_id, name, date_time, total_amount from customer natural join buy where buy_id = $buy_id";
    $result = mysqli_query($conn, $query);
    $buy = mysqli_fetch_assoc($result);
    if (!$buy) {
        msg("구매이력이 없습니다.");
    }
}

?>
    <div class="container fullwidth">

        <h3>구매 정보 상세 보기</h3>

        <p>
            <label for="buy_id">구입번호</label>
            <input readonly type="text" id="buy_id" name="buy_id" value="<?= $buy['buy_id'] ?>"/>
        </p>

        <p>
            <label for="customer_id">구입자 아이디</label>
            <input readonly type="text" id="customer_id" name="customer_id" value="<?= $buy['customer_id'] ?>"/>
        </p>

        <p>
            <label for="name">구입자명</label>
            <input readonly type="text" id="name" name="name" value="<?= $buy['name'] ?>"/>
        </p>

        <p>
            <label for="date_time">구입일자</label>
            <input readonly type="text" id="date_time" name="date_time" value="<?= $buy['date_time'] ?>"/>
        </p>
        <p>
            <label for="total_price">구입총액</label>
            <input readonly type="text" id="total_price" name="total_price" value="<?= $buy['total_amount'] ?>"/>
        </p>
    </div>
    
    <div class="container">
    
    <table class="table table-striped table-bordered">
        <tr>
            <th>No.</th>
            <th>제품번호</th>
            <th>제품명</th>
            <th>수량</th>
            <th>단가</th>
        </tr>
        
        <?
       
        $query = "select product_id, product_name, quantity, price from buy_item natural join product where buy_id = $buy_id";
        $result = mysqli_query($conn, $query);
        
        $row_num = mysqli_num_rows($result);
        for($row_index=1;$row_index<=$row_num;$row_index++){
            $row= mysqli_fetch_array($result);
            
            echo "<tr>";
            echo "<td>{$row_index}</td>";
            echo "<td>{$row['product_id']}</td>";
            echo "<td><a href='product_view.php?product_id={$row['product_id']}'>{$row['product_name']}</a></td>";
            echo "<td>{$row['quantity']}</td>";
            echo "<td>{$row['price']}</td>";
            echo "</tr>";
        }
        ?>
    </table>
</div>
    
<? include("footer.php") ?>