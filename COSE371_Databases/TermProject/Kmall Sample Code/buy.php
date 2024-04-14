<?
include "config.php";
include "util.php";
?>

<div class="container">

    <?
    $conn = dbconnect($host, $dbid, $dbpass, $dbname);
    
    $customer_id = $_POST['customer_id'];
    
    $available_insert = check_id($conn, $customer_id);
    if ($available_insert){
        $total_amount = 0;
        foreach($_POST['product_id'] as $pid){
            $query = "select price from product where product_id = $pid";
            $result = mysqli_query($conn, $query);
            $quantity = $_POST['quantity_'.$pid];
            $total_amount += (mysqli_fetch_array($result)[0] * $quantity);
        }
        // insert data into buy table.
        $query = "insert into buy (customer_id, date_time, total_amount) values ('$customer_id', NOW(), $total_amount)";
        mysqli_query($conn, $query);
        $buy_id = mysqli_insert_id($conn);
        
        // insert the data into buy_item table
        foreach($_POST['product_id'] as $pid){
        
            $quantity = $_POST['quantity_'.$pid];
            $query = "insert into buy_item(buy_id, product_id, quantity) values ($buy_id, $pid, $quantity)";
            mysqli_query($conn, $query);
        }
        s_msg('주문이 완료되었습니다');
        echo "<script>location.replace('buy_list.php');</script>";
    }
    else{
        msg('등록되지 않은 아이디 입니다.');
    }
    ?>

</div>

