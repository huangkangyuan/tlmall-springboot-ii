package fun.sherman.tlmall.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 购物车
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    /**
     * 是否勾选：1-已勾选，0-未勾选
     */
    private Integer checked;
    private java.util.Date createTime;
    private java.util.Date updateTime;
}
