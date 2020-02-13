package fun.sherman.tlmall.provider;

import fun.sherman.tlmall.domain.Shipping;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

/**
 * 收货地址模块动态sql
 *
 * @author sherman
 */
public class ShippingProvider extends SQL {
    private static final String TABLE_NAME = "tlmall_shipping";

    public String updateShippingProvider(Shipping shipping) {
        return new SQL() {
            {
                UPDATE(TABLE_NAME);
                if (StringUtils.isNotBlank(shipping.getReceiverName())) {
                    SET("receiver_name=#{receiverName}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverPhone())) {
                    SET("receiver_phone=#{receiverPhone}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverMobile())) {
                    SET("receiver_mobile=#{receiverMobile}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverProvince())) {
                    SET("receiver_province=#{receiverProvince}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverCity())) {
                    SET("receiver_city=#{receiverCity}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverDistrict())) {
                    SET("receiver_district=#{receiverDistrict}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverAddress())) {
                    SET("receiver_address=#{receiverAddress}");
                }
                if (StringUtils.isNotBlank(shipping.getReceiverZip())) {
                    SET("receiver_zip=#{receiverZip}");
                }
                if (shipping.getCreateTime() != null) {
                    SET("create_time=#{createTime}");
                }
                SET("update_time=now()");
                WHERE("user_id=#{userId} and id=#{id}");
            }
        }.toString();
    }
}
