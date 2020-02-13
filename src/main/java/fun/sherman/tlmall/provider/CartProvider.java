package fun.sherman.tlmall.provider;

import fun.sherman.tlmall.domain.Cart;
import org.apache.ibatis.jdbc.SQL;

/**
 * 购物车模块中动态sql
 *
 * @author sherman
 */
public class CartProvider extends SQL {
    private static final String TABLE_NAME = "tlmall_cart";

    public String updateCartProvider(Cart cart) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if (cart.getUserId() != null) {
                    SET("user_id = #{userId}");
                }
                if (cart.getProductId() != null) {
                    SET("product_id = #{productId}");
                }
                if (cart.getChecked() != null) {
                    SET("checked = #{checked}");
                }
                if (cart.getQuantity() != null) {
                    SET("quantity = #{quantity}");
                }
                if (cart.getCreateTime() != null) {
                    SET("create_time=#{createTime}");
                }
                SET("update_time = now()");
            }
        }.toString();
    }
}
