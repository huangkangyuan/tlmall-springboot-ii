package fun.sherman.tlmall.provider;

import fun.sherman.tlmall.domain.Product;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 产品模块动态sql
 *
 * @author sherman
 */
public class ProductProvider extends SQL {
    private static final String TABLE_NAME = "tlmall_product";

    public String updateProductProvider(Product product) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if (product.getCategoryId() != null) {
                    SET("category_id = #{categoryId}");
                }
                if (StringUtils.isNotBlank(product.getName())) {
                    SET("name = #{name}");
                }
                if (StringUtils.isNotBlank(product.getSubtitle())) {
                    SET("subtitle = #{subtitle}");
                }
                if (StringUtils.isNotBlank(product.getMainImage())) {
                    SET("main_image = #{mainImage}");
                }
                if (StringUtils.isNotBlank(product.getSubImages())) {
                    SET("sub_images = #{subImages}");
                }
                if (StringUtils.isNotBlank(product.getDetail())) {
                    SET("detail = #{detail}");
                }
                if (product.getPrice() != null) {
                    SET("price = #{price}");
                }
                if (product.getStock() != null) {
                    SET("stock = #{stock}");
                }
                if (product.getStatus() != null) {
                    SET("status = #{status}");
                }
                if (product.getCreateTime() != null) {
                    SET("create_time=#{createTime}");
                }
                SET("update_time = now()");
                WHERE("id = #{id}");
            }
        }.toString();
    }

    /**
     * Provider传入多个参数时，要使用Map进行封装，且key的数量是原参数的两倍(多出的参数名为：param1, param2...)
     *
     * @param maps 传入多个参数封装为Map对象
     * @return 动态select sql语句
     */
    public String selectProductProvider(Map<String, Object> maps) {
        return new SQL() {
            {
                SELECT("id, category_id, name, subtitle, main_image, sub_images, detail, price, stock, status, create_time, update_time");
                FROM(TABLE_NAME);
                if (maps.get("productName") != null) {
                    WHERE("name like #{productName}");
                }
                if (maps.get("productId") != null) {
                    WHERE("id = #{productId}");
                }
            }
        }.toString();
    }
}
