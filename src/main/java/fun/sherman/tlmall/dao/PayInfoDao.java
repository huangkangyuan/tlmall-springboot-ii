package fun.sherman.tlmall.dao;

import fun.sherman.tlmall.domain.PayInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

/**
 * @author sherman
 */
@Mapper
@Repository
public interface PayInfoDao {
    @Insert("insert into tlmall_pay_info(user_id, order_no, pay_platform, platform_status, platform_number, create_time, update_time) " +
            "values(#{userId}, #{orderNo}, #{payPlatform}, #{platformStatus}, #{platformNumber}, now(), now())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(PayInfo payInfo);
}
