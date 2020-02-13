package fun.sherman.tlmall.domain;

import lombok.*;

import java.math.BigDecimal;

/**
 * 订单模块
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Integer id;
    private Long orderNo;
    private Integer userId;
    private Integer shippingId;
    /**
     * 实际付款金额，单位元，保留两位小数
     */
    private BigDecimal payment;
    /**
     * 支付类型，1-在线支付，其它保留
     */
    private Integer paymentType;
    /**
     * 运费
     */
    private Integer postage;
    /**
     * 订单状态：0-已取消，10-未支付，20-已付款，40-已发货，50-交易成功，60-交易关闭
     */
    private Integer status;
    /**
     * 支付时间，支付成功，系统回调时间
     */
    private java.util.Date paymentTime;
    /**
     * 发货时间
     */
    private java.util.Date sendTime;
    /**
     * 交易完成时间
     */
    private java.util.Date endTime;
    /**
     * 交易关闭时间，下单但有效时间内未付款
     */
    private java.util.Date closeTime;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
