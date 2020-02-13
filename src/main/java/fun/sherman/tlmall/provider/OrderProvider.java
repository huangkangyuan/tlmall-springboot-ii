package fun.sherman.tlmall.provider;

import fun.sherman.tlmall.domain.Order;
import org.apache.ibatis.jdbc.SQL;

/**
 * 订单模块动态sql
 *
 * @author sherman
 */
public class OrderProvider extends SQL {
    private static final String TABLE_NAME = "tlmall_order";

    public String updateOrderProvider(Order order) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if (order.getOrderNo() != null) {
                    SET("order_no=#{orderNo}");
                }
                if (order.getUserId() != null) {
                    SET("user_id=#{userId}");
                }
                if (order.getShippingId() != null) {
                    SET("shipping_id=#{shippingId}");
                }
                if (order.getPayment() != null) {
                    SET("payment=#{payment}");
                }
                if (order.getPaymentType() != null) {
                    SET("payment_type=#{paymentType}");
                }
                if (order.getPostage() != null) {
                    SET("postage=#{postage}");
                }
                if (order.getStatus() != null) {
                    SET("status=#{status}");
                }
                if (order.getPaymentTime() != null) {
                    SET("payment_time=#{paymentTime}");
                }
                if (order.getSendTime() != null) {
                    SET("send_time=#{sendTime}");
                }
                if (order.getEndTime() != null) {
                    SET("end_time=#{endTime}");
                }
                if (order.getCloseTime() != null) {
                    SET("close_time=#{closeTime}");
                }
                if (order.getCreateTime() != null) {
                    SET("create_time=#{createTime}");
                }
                SET("update_time=now()");
                WHERE("id=#{id}");
            }
        }.toString();
    }
}
