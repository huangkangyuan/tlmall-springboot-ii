package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.Cart;
import fun.sherman.tlmall.provider.CartProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sherman
 */
@Mapper
@Repository
public interface CartDao {
    @Select("select id, user_id, product_id, quantity, checked, create_time, update_time from tlmall_cart " +
            "where user_id=#{userId} and product_id=#{productId}")
    Cart selectCartByUserIdAndProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    @UpdateProvider(type = CartProvider.class, method = "updateCartProvider")
    int updateCartSelective(Cart cart);

    @Insert("insert into tlmall_cart(user_id, product_id, checked, quantity, create_time, update_time) " +
            "values(#{userId}, #{productId}, #{checked}, #{quantity}, now(), now())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(Cart cart);

    @Select("select id, user_id, product_id, quantity, checked, create_time, update_time from tlmall_cart " +
            "where user_id=#{userId}")
    List<Cart> selectCartsByUserId(Integer userId);

    @Select("select count(1) from tlmall_cart where checked=0 and user_id=#{usedId}")
    int selectCartProductCheckedStatusByUserId(Integer userId);

    @Delete("<script>" +
            "delete from tlmall_cart where user_id=#{userId} " +
            "<if test = 'productIdList != null'> and product_id in " +
                "<foreach collection='productIdList' item='item' open='(' separator=',' close=')'> #{item} </foreach>" +
            "</if>" +
            "</script>")
    int deleteByUserIdAndProductIds(@Param("userId") Integer userId, @Param("productIdList") List<String> productIdList);

    @Update("<script>" +
            "update tlmall_cart set checked=#{checked}, update_time=now() where user_id=#{userId} " +
            "<if test = 'productId != null'> and product_id=#{productId} </if>" +
            "</script>")
    void checkOrUnCheck(@Param("userId") Integer userId, @Param("productId") Integer productId, @Param("checked") int checked);

    @Select("select IFNULL(sum(quantity), 0) as count from tlmall_cart where user_id=#{userId}")
    int selectCartProductCount(Integer userId);

    @Select("select id, user_id, product_id, quantity, create_time, update_time from tlmall_cart " +
            "where user_id=#{userId} and checked=1")
    List<Cart> selectCheckedCartsByUserId(Integer userId);

    @Delete("delete from tlmall_cart where id=#{id}")
    int deleteByPrimaryKey(Integer id);
}
