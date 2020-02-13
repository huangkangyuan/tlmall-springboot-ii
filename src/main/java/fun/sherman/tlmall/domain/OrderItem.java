package fun.sherman.tlmall.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 订单细明
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private Integer id;
    private Integer userId;
    private Long orderNo;
    private Integer productId;
    private String productName;
    private String productImage;
    /**
     * 生成的订单时单价，单位元
     */
    private BigDecimal currentUnitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
