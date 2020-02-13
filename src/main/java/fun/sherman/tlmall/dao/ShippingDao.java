package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.Shipping;
import fun.sherman.tlmall.provider.ShippingProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author sherman
 */
@Repository
@Mapper
public interface ShippingDao {
    @Insert("insert into tlmall_shipping(user_id, receiver_name, receiver_phone, receiver_mobile, receiver_province, receiver_city, " +
            "receiver_district, receiver_address, receiver_zip, create_time, update_time) values(#{userId}, #{receiverName}, #{receiverPhone}, " +
            "#{receiverMobile}, #{receiverProvince}, #{receiverCity}, #{receiverDistrict}, #{receiverAddress}, #{receiverZip}, now(), now())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(Shipping shipping);

    @Delete("delete from tlmall_shipping where user_id=#{userId} and id=#{shippingId}")
    int deleteByUserIdAndShippingId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);

    @UpdateProvider(type = ShippingProvider.class, method = "updateShippingProvider")
    int updateShippingSelective(Shipping shipping);

    @Select("select id, user_id, receiver_name, receiver_phone, receiver_mobile, receiver_province, receiver_city, receiver_district, receiver_address, receiver_zip, create_time, update_time " +
            "from tlmall_shipping where id=#{shippingId} and user_id=#{userId}")
    Shipping selectByShippingIdAndUserId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);

    @Select("select id, user_id, receiver_name, receiver_phone, receiver_mobile, receiver_province, receiver_city, receiver_district, receiver_address, receiver_zip, create_time, update_time " +
            "from tlmall_shipping where user_id=#{userId}")
    List<Shipping> listAllShippingInfoByUserId(Integer userId);

    @Select("select id, user_id, receiver_name, receiver_phone, receiver_mobile, receiver_province, receiver_city, receiver_district, receiver_address, receiver_zip, create_time, update_time " +
            "from tlmall_shipping where id=#{shippingId}")
    Shipping selectShippingByPrimaryKey(Integer shippingId);
}
