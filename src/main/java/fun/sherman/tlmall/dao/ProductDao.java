package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.Product;
import fun.sherman.tlmall.provider.ProductProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sherman
 */
@Mapper
@Repository
public interface ProductDao {
    @UpdateProvider(type = ProductProvider.class, method = "updateProductProvider")
    int updateProduct(Product product);

    @Insert("insert into tlmall_product(category_id, name, subtitle, main_image, sub_images, detail, price, stock, status, create_time, update_time) " +
            "values(#{categoryId}, #{name}, #{subtitle}, #{mainImage}, #{subImages}, #{detail}, #{price}, #{stock}, #{status}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertProduct(Product product);

    @Select("select id, category_id, name, subtitle, main_image, sub_images, detail, price, stock, status, create_time, update_time from tlmall_product where id = #{productId}")
    Product selectProductById(Integer productId);

    @Select("select id, category_id, name, subtitle, main_image, sub_images, detail, price, stock, status, create_time, update_time from tlmall_product order by id asc")
    List<Product> selectList();

    /**
     * 动态sql接受时，入参应该为Map<String, Object>类型
     */
    @SelectProvider(type = ProductProvider.class, method = "selectProductProvider")
    List<Product> searchByProductOrId(@Param("productName") String productName, @Param("productId") Integer productId);

    /**
     * 该查找结果返回给前端，因此只查询“在售”的商品
     */
    @Select("<script>" +
            "select id, category_id, name, subtitle, main_image, sub_images, detail, price, stock, status, create_time, update_time from tlmall_product " +
            "<where>" +
            "status = 1" +
            "<if test = 'keyword != null'> and name like #{keyword} </if>" +
            "<if test = 'categoryIdList != null'> and category_id in " +
            "<foreach collection='categoryIdList' item='item' open='(' separator=',' close=')'> #{item} </foreach>" +
            "</if>" +
            "</where>" +
            "</script>")
    List<Product> selectByKeywordAndCategoryIds(@Param("keyword") String keyword, @Param("categoryIdList") List<Integer> categoryIdList);

    @Select("select stock from tlmall_product where id=#{productId} for update")
    Integer selectStockByProductId(Integer productId);
}
