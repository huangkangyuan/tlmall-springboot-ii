package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.OrderItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sherman
 */
@Mapper
@Repository
public interface OrderItemDao {
    @Select("select id, user_id, order_no, product_id, product_name, product_image, current_unit_price, quantity, total_price, create_time, update_time from tlmall_order_item " +
            "where user_id=#{userId} and order_no=#{orderNo}")
    List<OrderItem> getOrderItemsByUserIdAndOrderNo(@Param("userId") Integer userId, @Param("orderNo") long orderNo);

    @Select("select id, user_id, order_no, product_id, product_name, product_image, current_unit_price, quantity, total_price, create_time, update_time from tlmall_order_item " +
            "where user_id=#{userId}")
    List<OrderItem> getOrderItemsByUserId(Integer userId);

    @Insert("<script>" +
            "insert into tlmall_order_item (order_no, user_id, product_id, product_name, product_image, current_unit_price, quantity, total_price, create_time, update_time) " +
            "values" +
            "<foreach collection='orderItemList' index='index' item='item' separator=','> " +
            "( #{item.orderNo}, #{item.userId}, #{item.productId}, #{item.productName}, #{item.productImage}, #{item.currentUnitPrice}, #{item.quantity}, #{item.totalPrice}, now(), now() ) " +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("orderItemList") List<OrderItem> orderItemList);

    @Select("select id, user_id, order_no, product_id, product_name, product_image, current_unit_price, quantity, total_price, create_time, update_time from tlmall_order_item " +
            "where order_no=#{orderNo}")
    List<OrderItem> getOrderItemsByOrderNo(Long orderNo);
}
